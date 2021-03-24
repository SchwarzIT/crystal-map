package com.kaufland

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

private val plainTypes = listOf(String::class.java.canonicalName, Int::class.java.canonicalName, "java.lang.Integer", "java.lang.Double", "java.lang.Boolean", Double::class.java.canonicalName, Long::class.java.canonicalName, BigDecimal::class.java.canonicalName, Boolean::class.java.canonicalName)

object ProcessingContext {

    lateinit var roundEnv: RoundEnvironment

    lateinit var env: ProcessingEnvironment

    val createdQualitfiedClazzNames: MutableSet<ClassName> = hashSetOf()


    fun Element.isAssignable(clazz: Class<*>) = ProcessingContext.env.let {
        it.typeUtils.isAssignable(asType(), it.elementUtils.getTypeElement(clazz.canonicalName).asType())
    }

    fun Element.asTypeElement(): TypeElement? = env.elementUtils.getTypeElement(ElementUtil.splitGenericIfNeeded(this.asType().toString())[0])

    fun Element.asDeclaringName(optinalIndexes: Array<Int>): DeclaringName = DeclaringName(this.asType(), 0, optinalIndexes)

    data class DeclaringName(private val typeMirror: TypeMirror, private val relevantIndex : Int = 0, private val nullableIndexes: Array<Int> = emptyArray()) {
        val name: String

        val typeParams: List<DeclaringName>

        init {
            ElementUtil.splitGenericIfNeeded(typeMirror.toString()).apply {
                name = this[0]
                val typeArgs: List<TypeMirror> = (typeMirror as? Type.ClassType?)?.let { it.typeArguments }
                        ?: emptyList()
                typeParams = typeArgs.mapIndexed { index, typeMirror -> DeclaringName(typeMirror, relevantIndex + index + 1, nullableIndexes) }
            }
        }

        fun asTypeName(): TypeName? = createdQualitfiedClazzNames.firstOrNull { it.simpleName == name }
                ?: if(isTypeVar()) TypeVariableName(name).copy(nullable = isNullable()) else asTypeElement()?.asClassName()?.javaToKotlinType()?.copy(nullable = isNullable())

        fun asFullTypeName(): TypeName? = asTypeName()?.let {
            if (it is ClassName && typeParams.isNotEmpty()) {
                it.parameterizedBy(typeParams.mapNotNull { if(it.isTypeVar()) TypeVariableName(it.name) else it.asFullTypeName() })
            } else it
        }


        fun hasEmptyConstructor() = (typeMirror as? Type.ClassType?)?.let { it.asElement().enclosedElements.any { it.getKind() == ElementKind.CONSTRUCTOR && (it as? Symbol.MethodSymbol)?.parameters?.size == 0 && !it.modifiers.contains(Modifier.PRIVATE) }} ?: false

        fun isPlainType() = plainTypes.contains(name)

        fun isTypeVar() = typeMirror is Type.TypeVar

        fun isNullable() = nullableIndexes.contains(relevantIndex)

        fun asTypeElement(): TypeElement? = (typeMirror as? PrimitiveType?)?.let { env.typeUtils.boxedClass(it) } ?: env.elementUtils.getTypeElement(name)

        fun isProcessingType(): Boolean {
            return createdQualitfiedClazzNames.any { it.simpleName == name } && name.let { it.endsWith("Wrapper") || it.endsWith("Entity") }
        }
    }

}