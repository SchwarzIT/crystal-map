package com.kaufland

import com.kaufland.util.ConversionUtil
import com.kaufland.util.ElementUtil
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asClassName
import com.sun.tools.javac.code.Symbol
import java.math.BigDecimal
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import com.sun.tools.javac.code.Type
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.type.PrimitiveType

private val plainTypes = listOf(String::class.java.canonicalName, Int::class.java.canonicalName, Double::class.java.canonicalName, Long::class.java.canonicalName, BigDecimal::class.java.canonicalName, Boolean::class.java.canonicalName)

object ProcessingContext {

    lateinit var roundEnv: RoundEnvironment

    lateinit var env: ProcessingEnvironment

    val createdQualitfiedClazzNames: MutableSet<ClassName> = hashSetOf()


    fun Element.isAssignable(clazz: Class<*>) = ProcessingContext.env.let {
        it.typeUtils.isAssignable(asType(), it.elementUtils.getTypeElement(clazz.canonicalName).asType())
    }

    fun Element.asTypeElement(): TypeElement? = env.elementUtils.getTypeElement(ElementUtil.splitGenericIfNeeded(this.asType().toString())[0])

    fun Element.asDeclaringName(): DeclaringName = DeclaringName(this.asType())

    data class DeclaringName(private val typeMirror: TypeMirror) {
        val name: String

        val typeParams: List<DeclaringName>

        init {
            ElementUtil.splitGenericIfNeeded(typeMirror.toString()).apply {
                name = this[0]
                val typeArgs: List<TypeMirror> = (typeMirror as? Type.ClassType?)?.let { it.typeArguments }
                        ?: emptyList()
                typeParams = typeArgs.map { DeclaringName(it) }
            }
        }

        fun asTypeName(): TypeName? = createdQualitfiedClazzNames.firstOrNull { it.simpleName == name }
                ?: asTypeElement()?.asClassName()?.javaToKotlinType()

        fun asFullTypeName(): TypeName? = asTypeName()?.let {
            if (it is ClassName && typeParams.isNotEmpty()) {
                it.parameterizedBy(typeParams.mapNotNull { if(it.isTypeVar()) TypeVariableName(it.name) else it.asFullTypeName() })
            } else it
        }


        fun hasEmptyConstructor() = (typeMirror as? Type.ClassType?)?.let { it.asElement().enclosedElements.any { it.getKind() == ElementKind.CONSTRUCTOR && (it as? Symbol.MethodSymbol)?.parameters?.size == 0 && !it.modifiers.contains(Modifier.PRIVATE) }} ?: false

        fun isPlainType() = plainTypes.contains(name)

        fun isTypeVar() = typeMirror is Type.TypeVar

        fun asTypeElement(): TypeElement? = (typeMirror as? PrimitiveType?)?.let { env.typeUtils.boxedClass(it) } ?: env.elementUtils.getTypeElement(name)

        fun isProcessingType(): Boolean {
            return createdQualitfiedClazzNames.any { it.simpleName == name } && name.let { it.endsWith("Wrapper") || it.endsWith("Entity") }
        }
    }

}