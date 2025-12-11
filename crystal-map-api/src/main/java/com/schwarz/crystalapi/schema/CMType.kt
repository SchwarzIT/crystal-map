package com.schwarz.crystalapi.schema

import com.schwarz.crystalapi.ITypeConverter

sealed interface CMType {
    val name: String
    val path: String
}

open class CMJsonField<T>(
    override val name: String,
    override val path: String,
) : CMType

open class CMJsonList<T>(
    override val name: String,
    override val path: String,
) : CMType

class CMObjectField<out T : Schema>(
    val element: T,
    override val name: String,
    override val path: String,
) : CMType

class CMObjectList<out T : Schema>(
    val element: T,
    override val name: String,
    override val path: String,
) : CMType

class CMConverterField<KotlinType, MapType>(
    name: String,
    path: String,
    val typeConverter: ITypeConverter<KotlinType, MapType>,
) : CMJsonField<MapType>(name, path)

class CMConverterList<KotlinType, MapType>(
    name: String,
    path: String,
    val typeConverter: ITypeConverter<KotlinType, MapType>,
) : CMJsonList<MapType>(name, path)
