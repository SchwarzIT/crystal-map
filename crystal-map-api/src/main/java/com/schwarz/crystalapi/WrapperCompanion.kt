package com.schwarz.crystalapi

abstract class WrapperCompanion<T : MapSupport> : CrystalCreator<T, Any?>() {

    fun fromMap(obj: MutableMap<String, Any?>?): T? {
        if(obj == null) {
            return null
        }
        return create(obj)
    }

    fun fromMap(obj: List<MutableMap<String, Any?>>?): List<T>? {
        if(obj == null) {
            return null
        }
        var result = ArrayList<T>()
        for(entry in obj) {
            result.add(create(entry))
        }
        return result
    }

    abstract fun toMap(obj: T?): MutableMap<String, Any>

    fun toMap(obj: List<T>?): List<MutableMap<String, Any>> {
        if(obj == null) {
            return listOf()
        }
        var result = ArrayList<MutableMap<String, Any>>()
        for(entry in obj) {
            var temp = mutableMapOf<String,Any>()
            temp.putAll(toMap(entry)!!)
            result.add(temp)
        }
        return result
    }

}