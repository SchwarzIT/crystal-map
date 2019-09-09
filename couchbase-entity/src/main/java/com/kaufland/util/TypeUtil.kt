package com.kaufland.util

import com.kaufland.javaToKotlinType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName

import java.util.ArrayList

import javax.lang.model.type.TypeMirror
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.asTypeName

object TypeUtil {

    fun string(): TypeName {
        return ClassName("kotlin", "String")
    }

    fun any(): TypeName {
        return ClassName("kotlin", "Any")
    }

    fun hashMapStringObject(): ParameterizedTypeName {
        return ClassName("kotlin.collections", "HashMap").parameterizedBy(string(), any())
    }

    fun mapStringObject(): ParameterizedTypeName {
        return ClassName("kotlin.collections", "Map").parameterizedBy(string(), any())
    }

    fun listWithMapStringObject(): ParameterizedTypeName {
        return ClassName("kotlin.collections", "List").parameterizedBy(mapStringObject())
    }

    fun list(typeName: TypeName): ParameterizedTypeName {
        return ClassName("kotlin.collections", "List").parameterizedBy(typeName)
    }

    fun arrayList(typeName: TypeName): ParameterizedTypeName {
        return ClassName("kotlin.collections", "ArrayList").parameterizedBy(typeName)
    }

    fun arrayListWithMapStringObject(): ParameterizedTypeName {
        return ClassName("kotlin.collections", "ArrayList").parameterizedBy(mapStringObject())
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
        try {
            baseType = ClassName(getPackage(type), simpleName)
        } catch (e: IllegalArgumentException) {
            baseType = type.asTypeName()
        }



        return if (list) {
            list(baseType!!.javaToKotlinType())
        } else baseType!!.javaToKotlinType()
    }
}
