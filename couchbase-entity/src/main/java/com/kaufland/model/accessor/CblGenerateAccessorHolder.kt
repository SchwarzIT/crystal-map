package com.kaufland.model.accessor

import com.kaufland.javaToKotlinType
import com.kaufland.util.ConversionUtil
import com.kaufland.util.FieldExtractionUtil
import com.kaufland.util.TypeUtil
import com.squareup.kotlinpoet.*
import kaufland.com.coachbasebinderapi.Field
import org.apache.commons.lang3.text.WordUtils
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.type.TypeMirror

class CblGenerateAccessorHolder(private val className: String, val element: Element) {

    fun accessorFunSpec(): FunSpec? {

        if(element.kind == ElementKind.METHOD){
            var methodBuilder = FunSpec.builder(element.simpleName.toString()).addAnnotation(JvmStatic::class)

            (element as ExecutableElement)?.apply {
                parameters.forEach {
                    methodBuilder.addParameter(it.simpleName.toString(), it.asType().asTypeName().javaToKotlinType())
                }
                methodBuilder.addStatement("%N.%N(${parameters.joinToString { it.simpleName.toString() }})", className, element.simpleName.toString())
            }
            return methodBuilder.build()
        }
        return null
    }


    fun accessorPropertySpec(): PropertySpec? {
        if(element.kind == ElementKind.FIELD){
           return PropertySpec.builder(element.simpleName.toString(), element.asType().asTypeName().javaToKotlinType()).addAnnotation(JvmField::class).initializer("%N.%N", className, element.simpleName.toString()).build()
        }
        return null
    }
}
