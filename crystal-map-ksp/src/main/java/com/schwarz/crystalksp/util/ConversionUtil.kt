package com.schwarz.crystalksp.util

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType
import kotlin.reflect.KClass

fun KSDeclaration.getAnnotation(annotationClass: KClass<out Annotation>): KSAnnotation? {
    val targetAnnotationQualifiedName = annotationClass.qualifiedName ?: return null

    return this.annotations.firstOrNull { ksAnnotation ->
        ksAnnotation.annotationType.resolve().declaration.qualifiedName?.asString() == targetAnnotationQualifiedName
    }
}

fun <T> KSAnnotation.getArgument(argumentName: String): T? {
    return this.arguments.firstOrNull { it.name?.getShortName() == argumentName }?.value as? T
}

inline fun <reified T : Enum<T>> KSAnnotation.getEnumArgument(argumentName: String): T? {
    val argument = this.getArgument<Any>(argumentName)
    return when (argument) {
        is KSType -> {
            for (enumValue in enumValues<T>()) {
                if (enumValue.name == argument.declaration.simpleName.asString()) {
                    return enumValue
                }
            }
            return null
        }
        is KSClassDeclaration -> {
            for (enumValue in enumValues<T>()) {
                if (enumValue.name == argument.simpleName.asString()) {
                    return enumValue
                }
            }
            return null
        }
        else -> {
            null
        }
    }
}
