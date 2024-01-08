package com.schwarz.crystalapi

abstract class CrystalCreator<T : MapSupport, V> {

    abstract fun create(): T

    abstract fun create(map: MutableMap<String, V>): T
}
