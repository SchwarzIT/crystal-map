package com.kaufland.model.accessor

import com.kaufland.javaToKotlinType
import com.squareup.kotlinpoet.*
import org.jetbrains.annotations.Nullable
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.type.TypeMirror

class CblGenerateAccessorHolder(private val className: String, val element: Element) {

    fun accessorFunSpec(): FunSpec? {

        if(element.kind == ElementKind.METHOD){
            var methodBuilder = FunSpec.builder(element.simpleName.toString()).addAnnotation(JvmStatic::class)

            (element as ExecutableElement)?.apply {
                methodBuilder.returns(evaluateTypeName(returnType, element.getAnnotation(Nullable::class.java) != null))
                parameters.forEach {
                    methodBuilder.addParameter(it.simpleName.toString(), evaluateTypeName(it.asType(), it.getAnnotation(Nullable::class.java) != null))
                }
                methodBuilder.addStatement("return %N.%N(${parameters.joinToString { it.simpleName.toString() }})", className, element.simpleName.toString())
            }
            return methodBuilder.build()
        }
        return null
    }

    private fun evaluateTypeName(typeMirror: TypeMirror, nullable: Boolean): TypeName {
        return typeMirror.asTypeName().javaToKotlinType().copy(nullable = nullable)
    }

    fun accessorPropertySpec(): PropertySpec? {
        if(element.kind == ElementKind.FIELD){
           return PropertySpec.builder(element.simpleName.toString(), evaluateTypeName(element.asType(), element.getAnnotation(Nullable::class.java) != null)).addAnnotation(JvmField::class).initializer("%N.%N", className, element.simpleName.toString()).build()
        }
        return null
    }
}
