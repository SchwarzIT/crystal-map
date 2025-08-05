package com.schwarz.crystalprocessor

import com.schwarz.crystalapi.mapify.Mapifyable
import com.schwarz.crystalcore.javaToKotlinType
import com.schwarz.crystalcore.model.source.ISourceDeclaringName
import com.schwarz.crystalprocessor.util.ElementUtil
import com.schwarz.crystalprocessor.util.FieldExtractionUtil
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
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
import javax.tools.Diagnostic

private val plainTypes = listOf(String::class.java.canonicalName, Int::class.java.canonicalName, "java.lang.Integer", "java.lang.Double", "java.lang.Boolean", Double::class.java.canonicalName, Long::class.java.canonicalName, BigDecimal::class.java.canonicalName, Boolean::class.java.canonicalName)

object ProcessingContext {

    lateinit var roundEnv: RoundEnvironment

    lateinit var env: ProcessingEnvironment

    val createdQualifiedClazzNames: MutableSet<ClassName> = hashSetOf()

    fun Element.asDeclaringName(optinalIndexes: Array<Int>): DeclaringName = DeclaringName(this.asType(), 0, optinalIndexes)

    data class DeclaringName(private val typeMirror: TypeMirror, private val relevantIndex: Int = 0, private val nullableIndexes: Array<Int> = emptyArray()) : ISourceDeclaringName {
        override val name: String

        override val typeParams: List<DeclaringName>

        init {
            ElementUtil.splitGenericIfNeeded(typeMirror.toString()).apply {
                name = this[0]
                val typeArgs: List<TypeMirror> = (typeMirror as? Type.ClassType?)?.let { it.typeArguments }
                    ?: emptyList()
                typeParams = typeArgs.mapIndexed { index, typeMirror -> DeclaringName(typeMirror, relevantIndex + index + 1, nullableIndexes) }
            }
        }

        override fun asTypeName(): TypeName? = createdQualifiedClazzNames.firstOrNull { it.simpleName == name }
            ?: if (isTypeVar()) TypeVariableName(name).copy(nullable = isNullable()) else asTypeElement()?.asClassName()?.javaToKotlinType()?.copy(nullable = isNullable())

        override fun asFullTypeName(): TypeName? = asTypeName()?.let {
            if (it is ClassName && typeParams.isNotEmpty()) {
                it.parameterizedBy(typeParams.mapNotNull { if (it.isTypeVar()) TypeVariableName(it.name) else it.asFullTypeName() })
            } else {
                it
            }
        }

        override fun hasEmptyConstructor() = (typeMirror as? Type.ClassType?)?.let {
            it.asElement().enclosedElements.any {
                it.getKind() == ElementKind.CONSTRUCTOR && (it as? Symbol.MethodSymbol)?.parameters?.size == 0 && !it.modifiers.contains(Modifier.PRIVATE)
            }
        } ?: false

        override fun isPlainType() = plainTypes.contains(name)

        override fun isTypeVar(): Boolean {
            return typeMirror is Type.TypeVar
        }

        override fun isNullable() = nullableIndexes.contains(relevantIndex)

        override fun isProcessingType(): Boolean {
            return createdQualifiedClazzNames.any { it.simpleName == name } && name.let { it.endsWith("Wrapper") || it.endsWith("Entity") }
        }

        override fun isAssignable(clazz: Class<*>) = env.let {
            return@let asTypeElement()?.let {
                env.typeUtils.isAssignable(it.asType(), env.elementUtils.getTypeElement(clazz.canonicalName).asType())
            } ?: false
        }

        override fun <A : Annotation?> getAnnotation(annotationType: Class<A>?): A? {
            env.let {
                return it.typeUtils.asElement(typeMirror).getAnnotation(annotationType)
            }
        }

        override fun typeAsDeclaringName(mapifyable: Mapifyable): ISourceDeclaringName? {
            FieldExtractionUtil.typeMirror(mapifyable)?.apply {
                return DeclaringName(this)
            }
            return null
        }

        private fun asTypeElement(): TypeElement? = (typeMirror as? PrimitiveType?)?.let { env.typeUtils.boxedClass(it) } ?: env.elementUtils.getTypeElement(ElementUtil.splitGenericIfNeeded(typeMirror.toString())[0]) ?: env.elementUtils.getTypeElement(name)
    }
}
