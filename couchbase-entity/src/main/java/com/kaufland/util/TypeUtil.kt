package com.kaufland.util

import com.kaufland.javaToKotlinType
import com.squareup.kotlinpoet.*

import java.util.ArrayList

import javax.lang.model.type.TypeMirror
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

object TypeUtil {

    fun string(): TypeName {
        return ClassName("kotlin", "String")
    }

    fun any(): TypeName {
        return ClassName("kotlin", "Any")
    }

    fun anyNullable(): TypeName {
        return any().copy(nullable = true)
    }

    fun star(): TypeName {
        return WildcardTypeName.producerOf(anyNullable())
    }

    fun hashMapStringObject(): ParameterizedTypeName {
        return ClassName("kotlin.collections", "HashMap").parameterizedBy(string(), anyNullable())
    }

    fun mapStringObject(): ParameterizedTypeName {
        return ClassName("kotlin.collections", "Map").parameterizedBy(string(), anyNullable())
    }

    fun mutableMapStringObject(): ParameterizedTypeName {
        return ClassName("kotlin.collections", "MutableMap").parameterizedBy(string(), anyNullable())
    }

    fun listWithMutableMapStringObject(): ParameterizedTypeName {
        return ClassName("kotlin.collections", "List").parameterizedBy(mutableMapStringObject())
    }

    fun list(typeName: TypeName): ParameterizedTypeName {
        return ClassName("kotlin.collections", "List").parameterizedBy(typeName)
    }

    fun arrayList(typeName: TypeName): ParameterizedTypeName {
        return ClassName("kotlin.collections", "ArrayList").parameterizedBy(typeName)
    }

    fun arrayListWithHashMapStringObject(): ParameterizedTypeName {
        return ClassName("kotlin.collections", "ArrayList").parameterizedBy(hashMapStringObject())
    }

    fun mapSupport(): TypeName {
        return ClassName("kaufland.com.coachbasebinderapi", "MapSupport")
    }

    fun getSimpleName(type: TypeMirror): String {
        val parts = type.toString().split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return if (parts.size > 1) parts[parts.size - 1] else parts[0]
    }

    fun getPackage(type: TypeMirror): String {
        val lastIndexOf = type.toString().lastIndexOf(".")
        return if (lastIndexOf >= 0) type.toString().substring(0, lastIndexOf) else type.toString()
    }

    fun parseMetaType(type: TypeMirror, list: Boolean, subEntity: String?): TypeName {

        val simpleName = if (subEntity != null && subEntity.contains(getSimpleName(type))) subEntity else getSimpleName(type)

        var baseType: TypeName? = null

        if (type.toString().split(".").size == 1) {
            baseType = type.asTypeName()
        } else {
            try {
                baseType = ClassName(getPackage(type), simpleName)
            } catch (e: IllegalArgumentException) {
                baseType = type.asTypeName()
            }
        }

        return if (list) {
            list(baseType!!.javaToKotlinType())
        } else baseType!!.javaToKotlinType()
    }

    fun classStar(): ParameterizedTypeName {
        return ClassName("kotlin.reflect", "KClass").parameterizedBy(star())
    }
}
