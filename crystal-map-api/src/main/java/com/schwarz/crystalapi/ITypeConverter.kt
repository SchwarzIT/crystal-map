package com.schwarz.crystalapi

interface ITypeConverter<KotlinType, MapType> {
    fun write(value: KotlinType?): MapType?

    fun read(value: MapType?): KotlinType?
}
