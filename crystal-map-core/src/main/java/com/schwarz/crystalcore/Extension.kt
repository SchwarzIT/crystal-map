package com.schwarz.crystalcore

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import kotlin.reflect.jvm.internal.impl.builtins.jvm.JavaToKotlinClassMap
import kotlin.reflect.jvm.internal.impl.name.FqName
import kotlin.reflect.jvm.internal.impl.name.FqNameUnsafe

fun TypeName.javaToKotlinType(): TypeName =
    if (this is ParameterizedTypeName) {
        (rawType.javaToKotlinType() as ClassName).parameterizedBy(
            *typeArguments.map { it.javaToKotlinType() }.toTypedArray()
        )
    } else {
        val className =
            JavaToKotlinClassMap.INSTANCE
                .mapJavaToKotlin(FqName(toString()))?.asSingleFqName()?.asString()
        if (className == null) {
            this
        } else {
            ClassName.bestGuess(className)
        }
    }

fun TypeName.kotlinToJavaType(): TypeName =
    if (this is ParameterizedTypeName) {
        (rawType.kotlinToJavaType() as ClassName).parameterizedBy(
            *typeArguments.map { it.kotlinToJavaType() }.toTypedArray()
        )
    } else {
        val className =
            JavaToKotlinClassMap.INSTANCE
                .mapKotlinToJava(FqNameUnsafe(toString()))?.asSingleFqName()?.asString()
        if (className == null) {
            this
        } else {
            ClassName.bestGuess(className)
        }
    }
