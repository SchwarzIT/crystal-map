package com.schwarz.crystalapi.mapify

interface IMapifyable<T> {

    fun fromMap(map: Map<String, Any>): T

    fun toMap(obj: T): Map<String, Any>

    fun fromMap(list: List<Map<String, Any>>): List<T> = list.map { fromMap(it) }

    fun toMap(list: List<T>): List<Map<String, Any>> = list.map { toMap(it) }
}
