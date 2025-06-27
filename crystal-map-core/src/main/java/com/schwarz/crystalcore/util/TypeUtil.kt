package com.schwarz.crystalcore.util

import com.schwarz.crystalapi.CrystalCreator
import com.schwarz.crystalapi.IEntity
import com.schwarz.crystalapi.MapSupport
import com.schwarz.crystalapi.WrapperCompanion
import com.schwarz.crystalapi.mapify.IMapifyable
import com.schwarz.crystalapi.mapify.IMapper
import com.schwarz.crystalapi.mapify.Mapifyable
import com.schwarz.crystalapi.util.SerializableMapifyable
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.asTypeName

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

    fun map(): ClassName {
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

    fun crystalCreator(valueType: TypeName, type: TypeName): ParameterizedTypeName {
        return CrystalCreator::class.asTypeName().parameterizedBy(listOf(type, valueType))
    }

    fun wrapperCompanion(type: TypeName): ParameterizedTypeName {
        return WrapperCompanion::class.asTypeName().parameterizedBy(type)
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

    fun iMapifyable(typename: TypeName): TypeName {
        return IMapifyable::class.asTypeName().parameterizedBy(typename)
    }

    fun mapifyable(): TypeName {
        return Mapifyable::class.asTypeName()
    }

    fun serializableMapifyable(typename: TypeName): TypeName {
        return SerializableMapifyable::class.asTypeName().parameterizedBy(typename)
    }

    fun iEntity(): TypeName {
        return IEntity::class.asTypeName()
    }

    fun clazz(typename: TypeName): TypeName {
        return ClassName("java.lang", "Class").parameterizedBy(typename)
    }

    fun iDocId(): TypeName {
        return ClassName("com.schwarz.crystalapi", "IDocId")
    }

    fun classStar(): ParameterizedTypeName {
        return ClassName("kotlin.reflect", "KClass").parameterizedBy(star())
    }

    fun isMap(fieldType: TypeName): Boolean {
        return fieldType.toString().startsWith("kotlin.collections.Map")
    }

    fun mapOf() = MemberName("kotlin.collections", "mapOf")

    fun mutableMapOf() = MemberName("kotlin.collections", "mutableMapOf")

    fun arrayOf() = MemberName("kotlin", "arrayOf")
}
