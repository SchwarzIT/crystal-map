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
    fun string(): TypeName = ClassName("kotlin", "String")

    fun any(): TypeName = ClassName("kotlin", "Any")

    fun anyNullable(): TypeName = any().copy(nullable = true)

    fun star(): TypeName = WildcardTypeName.producerOf(anyNullable())

    fun map(): ClassName = ClassName("kotlin.collections", "Map")

    fun hashMapStringAnyNullable(): ParameterizedTypeName =
        ClassName("kotlin.collections", "HashMap").parameterizedBy(string(), anyNullable())

    fun linkedHashMapStringAnyNullable(): ParameterizedTypeName =
        ClassName("kotlin.collections", "LinkedHashMap").parameterizedBy(string(), anyNullable())

    fun hashMapStringAny(): ParameterizedTypeName =
        ClassName("kotlin.collections", "HashMap").parameterizedBy(string(), any())

    fun mapStringAnyNullable(): ParameterizedTypeName =
        map().parameterizedBy(string(), anyNullable())

    fun mapStringAny(): ParameterizedTypeName = map().parameterizedBy(string(), any())

    fun mapAnyAny(): ParameterizedTypeName = map().parameterizedBy(any(), any())

    fun mutableMapStringAnyNullable(): ParameterizedTypeName =
        ClassName("kotlin.collections", "MutableMap").parameterizedBy(string(), anyNullable())

    fun mutableMapStringAny(): ParameterizedTypeName =
        ClassName("kotlin.collections", "MutableMap").parameterizedBy(string(), any())

    fun listWithMutableMapStringAnyNullable(): ParameterizedTypeName =
        ClassName("kotlin.collections", "List").parameterizedBy(mutableMapStringAnyNullable())

    fun listWithMutableMapStringAny(): ParameterizedTypeName =
        ClassName("kotlin.collections", "List").parameterizedBy(mutableMapStringAny())

    fun list(typeName: TypeName): ParameterizedTypeName =
        ClassName("kotlin.collections", "List").parameterizedBy(typeName)

    fun crystalCreator(
        valueType: TypeName,
        type: TypeName,
    ): ParameterizedTypeName =
        CrystalCreator::class.asTypeName().parameterizedBy(listOf(type, valueType))

    fun wrapperCompanion(type: TypeName): ParameterizedTypeName =
        WrapperCompanion::class.asTypeName().parameterizedBy(type)

    fun arrayList(typeName: TypeName): ParameterizedTypeName =
        ClassName("kotlin.collections", "ArrayList").parameterizedBy(typeName)

    fun arrayListWithHashMapStringAnyNullable(): ParameterizedTypeName =
        ClassName("kotlin.collections", "ArrayList").parameterizedBy(hashMapStringAnyNullable())

    fun arrayListWithMutableMapStringAny(): ParameterizedTypeName =
        ClassName("kotlin.collections", "ArrayList").parameterizedBy(mutableMapStringAny())

    fun mapSupport(): TypeName = MapSupport::class.asTypeName()

    fun iMapper(typename: TypeName): TypeName =
        IMapper::class.asTypeName().parameterizedBy(typename)

    fun iMapifyable(typename: TypeName): TypeName =
        IMapifyable::class.asTypeName().parameterizedBy(typename)

    fun mapifyable(): TypeName = Mapifyable::class.asTypeName()

    fun serializableMapifyable(typename: TypeName): TypeName =
        SerializableMapifyable::class.asTypeName().parameterizedBy(typename)

    fun iEntity(): TypeName = IEntity::class.asTypeName()

    fun clazz(typename: TypeName): TypeName =
        ClassName("java.lang", "Class").parameterizedBy(typename)

    fun iDocId(): TypeName = ClassName("com.schwarz.crystalapi", "IDocId")

    fun classStar(): ParameterizedTypeName =
        ClassName("kotlin.reflect", "KClass").parameterizedBy(star())

    fun isMap(fieldType: TypeName): Boolean =
        fieldType.toString().startsWith("kotlin.collections.Map")

    fun mapOf() = MemberName("kotlin.collections", "mapOf")

    fun mutableMapOf() = MemberName("kotlin.collections", "mutableMapOf")

    fun arrayOf() = MemberName("kotlin", "arrayOf")
}
