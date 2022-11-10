package com.schwarz.crystalapi.mapify

interface IMapper<T> {

    fun fromMap(obj: T, map: Map<String, Any>)

    fun toMap(obj: T): Map<String, Any>
}
