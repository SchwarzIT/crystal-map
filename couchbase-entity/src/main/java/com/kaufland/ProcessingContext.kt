package com.kaufland

import com.kaufland.util.ConversionUtil
import com.kaufland.util.ElementUtil
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import java.math.BigDecimal
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

private val plainTypes = listOf(String::class.java.canonicalName, Int::class.java.canonicalName, Double::class.java.canonicalName, Long::class.java.canonicalName, BigDecimal::class.java.canonicalName)
object ProcessingContext {

    lateinit var roundEnv: RoundEnvironment

    lateinit var env: ProcessingEnvironment

    val createdQualitfiedClazzNames: MutableMap<String, TypeName> = hashMapOf()


    fun Element.isAssignable(clazz: Class<*>) = ProcessingContext.env.let {
        it.typeUtils.isAssignable(asType(), it.elementUtils.getTypeElement(clazz.canonicalName).asType())
    }

    fun Element.asTypeElement(): TypeElement? = env.elementUtils.getTypeElement(ElementUtil.splitGenericIfNeeded(this.asType().toString())[0])

    fun Element.asDeclaringName() : DeclaringName = DeclaringName(this.asType().toString())

    data class DeclaringName(private val full: String) {
        val name: String

        val typeParams: List<DeclaringName>

        init {
            ElementUtil.splitGenericIfNeeded(full).apply {
                name = this[0]
                val stringTypes: List<String> = if (size > 1) {
                    this.subList(1, this.size)
                } else listOf()
                typeParams = stringTypes.map { DeclaringName(it) }
            }
        }

        fun asTypeName() : TypeName? = createdQualitfiedClazzNames[name] ?: asTypeElement()?.asClassName()?.javaToKotlinType()

        fun asFullTypeName() : TypeName? = asTypeName()?.let {
            if(it is ClassName && typeParams.isNotEmpty()){
                it.parameterizedBy(typeParams.mapNotNull { it.asFullTypeName() })
            }else it
        }

        fun isPlainType() = plainTypes.contains(name)

        fun asTypeElement() : TypeElement? = env.elementUtils.getTypeElement(name)

        fun isProcessingType() : Boolean = createdQualitfiedClazzNames.containsKey(name) && name.let { it.endsWith("Wrapper") || it.endsWith("Entity") }
    }

}