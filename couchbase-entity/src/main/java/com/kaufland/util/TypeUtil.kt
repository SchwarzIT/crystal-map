package com.kaufland.util

import com.kaufland.javaToKotlinType
import com.squareup.kotlinpoet.*

import javax.lang.model.type.TypeMirror
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import kaufland.com.coachbasebinderapi.IEntity
import kaufland.com.coachbasebinderapi.MapSupport
import kaufland.com.coachbasebinderapi.mapify.IMapper
import kaufland.com.coachbasebinderapi.mapify.IMapifyable
import kaufland.com.coachbasebinderapi.mapify.Mapifyable
import kaufland.com.coachbasebinderapi.util.SerializableMapifyable

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

    fun map() : ClassName {
        return ClassName("kotlin.collections", "Map")
    }

    fun hashMapStringAnyNullable(): ParameterizedTypeName {
        return ClassName("kotlin.collections", "HashMap").parameterizedBy(string(), anyNullable())
    }

    fun linkedHashMapStringAnyNullable(): ParameterizedTypeName {
        return ClassName("kotlin.collections", "LinkedHashMap").parameterizedBy(string(), anyNullable())
    }

    fun hashMapStringAny(): ParameterizedTypeName {
        return ClassName("kotlin.collections", "HashMap").parameterizedBy(string(), any())
    }

    fun mapStringAnyNullable(): ParameterizedTypeName {
        return map().parameterizedBy(string(), anyNullable())
    }

    fun mapStringAny(): ParameterizedTypeName {
        return map().parameterizedBy(string(), any())
    }

    fun mapAnyAny(): ParameterizedTypeName {
        return map().parameterizedBy(any(), any())
    }

    fun mutableMapStringAnyNullable(): ParameterizedTypeName {
        return ClassName("kotlin.collections", "MutableMap").parameterizedBy(string(), anyNullable())
    }

    fun mutableMapStringAny(): ParameterizedTypeName {
        return ClassName("kotlin.collections", "MutableMap").parameterizedBy(string(), any())
    }

    fun listWithMutableMapStringAnyNullable(): ParameterizedTypeName {
        return ClassName("kotlin.collections", "List").parameterizedBy(mutableMapStringAnyNullable())
    }

    fun listWithMutableMapStringAny(): ParameterizedTypeName {
        return ClassName("kotlin.collections", "List").parameterizedBy(mutableMapStringAny())
    }

    fun list(typeName: TypeName): ParameterizedTypeName {
        return ClassName("kotlin.collections", "List").parameterizedBy(typeName)
    }

    fun arrayList(typeName: TypeName): ParameterizedTypeName {
        return ClassName("kotlin.collections", "ArrayList").parameterizedBy(typeName)
    }

    fun arrayListWithHashMapStringAnyNullable(): ParameterizedTypeName {
        return ClassName("kotlin.collections", "ArrayList").parameterizedBy(hashMapStringAnyNullable())
    }

    fun arrayListWithMutableMapStringAny(): ParameterizedTypeName {
        return ClassName("kotlin.collections", "ArrayList").parameterizedBy(mutableMapStringAny())
    }

    fun mapSupport(): TypeName {
        return MapSupport::class.asTypeName()
    }

    fun iMapper(typename: TypeName): TypeName {
        return IMapper::class.asTypeName().parameterizedBy(typename)
    }

    fun iMapifyable(typename: TypeName) : TypeName {
        return IMapifyable::class.asTypeName().parameterizedBy(typename)
    }

    fun mapifyable() : TypeName {
        return Mapifyable::class.asTypeName()
    }

    fun serializableMapifyable(typename: TypeName) : TypeName{
        return SerializableMapifyable::class.asTypeName().parameterizedBy(typename)
    }

    fun iEntity(): TypeName {
        return IEntity::class.asTypeName()
    }

    fun clazz(typename: TypeName) : TypeName{
        return ClassName("java.lang", "Class").parameterizedBy(typename)
    }

    fun iDocId(): TypeName {
        return ClassName("kaufland.com.coachbasebinderapi", "IDocId")
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
        return parseMetaType(type, list, true, subEntity)
    }

    fun parseMetaType(type: TypeMirror, list: Boolean, convertMap: Boolean, subEntity: String?): TypeName {

        val simpleName = if (subEntity != null && subEntity.contains(getSimpleName(type))) subEntity else getSimpleName(type)

        var baseType: TypeName?

        if (type.toString().split(".").size == 1) {
            baseType = type.asTypeName()
        } else {
            try {
                baseType = ClassName(getPackage(type), simpleName)
            } catch (e: IllegalArgumentException) {
                baseType = type.asTypeName()
            }
        }

        if(convertMap && baseType!!.javaToKotlinType() == map()){
            baseType = mapStringAnyNullable()
        }

        return if (list) {
            list(baseType!!.javaToKotlinType())
        } else baseType!!.javaToKotlinType()
    }

    fun classStar(): ParameterizedTypeName {
        return ClassName("kotlin.reflect", "KClass").parameterizedBy(star())
    }

    fun isMap(fieldType: TypeName): Boolean {
        return fieldType.toString().startsWith("kotlin.collections.Map")
    }
}
