package com.schwarz.crystalapi.schema

import com.schwarz.crystalapi.ITypeConverter

sealed interface CMType {
    val path: String
}

class CMField<T : Any>(val name: String, override val path: String) : CMType

class CMList<T : Any>(val name: String, override val path: String) : CMType

class CMObject<out T : Schema>(val element: T, override val path: String) : CMType

class CMObjectList<out T : Schema>(val element: T, val name: String, override val path: String) : CMType

class CMConverterField<KotlinType, MapType>(
    val name: String,
    val typeConverter: ITypeConverter<KotlinType, MapType>,
    override val path: String
) : CMType

class CMConverterList<KotlinType, MapType>(
    val name: String,
    val typeConverter: ITypeConverter<KotlinType, MapType>,
    override val path: String
) : CMType
