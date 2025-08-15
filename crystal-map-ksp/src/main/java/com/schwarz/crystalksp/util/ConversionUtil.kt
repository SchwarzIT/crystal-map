package com.schwarz.crystalksp.util

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSReferenceElement
import com.google.devtools.ksp.symbol.KSType
import kotlin.reflect.KClass

fun KSDeclaration.getAnnotation(annotationClass: KClass<out Annotation>): KSAnnotation? {
    val targetAnnotationQualifiedName = annotationClass.qualifiedName ?: return null

    return this.annotations.firstOrNull { ksAnnotation ->
        ksAnnotation.annotationType.resolve().declaration.qualifiedName?.asString() == targetAnnotationQualifiedName
    }
}

fun <T>KSAnnotation.getArgument(argumentName: String): T? {
    return this.arguments.firstOrNull { it.name?.getShortName() == argumentName }?.value as? T
}

inline fun <reified T : Enum<T>>KSAnnotation.getEnumArgument(argumentName: String): T? {
    return getArgument<KSType>(argumentName)?.let { enumClass ->
        for (enumValue in enumValues<T>()) {
            if(enumValue.name == enumClass.declaration.simpleName.asString()){
                return@let enumValue
            }
        }
        return@let null
    }
}