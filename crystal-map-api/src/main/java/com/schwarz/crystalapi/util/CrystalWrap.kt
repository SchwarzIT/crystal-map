@file:Suppress("UNCHECKED_CAST")

package com.schwarz.crystalapi.util

import com.schwarz.crystalapi.PersistenceConfig
import java.lang.Exception
import kotlin.reflect.KClass

object CrystalWrap {

    inline fun <T> get(
        changes: MutableMap<String, Any?>,
        doc: MutableMap<String, Any>,
        fieldName: String,
        clazz: KClass<*>,
        noinline mapper: ((MutableMap<String, Any?>?) -> T?)? = null
    ): T? {
        return (changes[fieldName] ?: doc[fieldName])?.let { value ->
            mapper?.let {
                mapper.invoke(value as? MutableMap<String, Any?>)
            } ?: read(value, fieldName, clazz)
        } ?: null
    }

    inline fun <T> getList(
        changes: MutableMap<String, Any?>,
        doc: MutableMap<String, Any>,
        fieldName: String,
        clazz: KClass<*>,
        noinline mapper: ((List<MutableMap<String, Any?>>?) -> List<T>)? = null
    ): List<T>? {
        return (changes[fieldName] ?: doc[fieldName])?.let { value ->
            mapper?.let {
                mapper.invoke(value as? List<MutableMap<String, Any?>>)
            } ?: read(value, fieldName, clazz)
        } ?: null
    }

    fun <T> set(
        changes: MutableMap<String, Any?>,
        fieldName: String,
        value: T,
        clazz: KClass<*>,
        mapper: ((T) -> MutableMap<String, Any>)? = null
    ) {
        val valueToSet = mapper?.let { it.invoke(value) } ?: write<T>(value, fieldName, clazz)
        changes[fieldName] = valueToSet
    }

    inline fun <T> setList(
        changes: MutableMap<String, Any?>,
        fieldName: String,
        value: List<T>?,
        clazz: KClass<*>,
        noinline mapper: ((List<T>) -> List<MutableMap<String, Any>>)? = null
    ) {
        val valueToSet =
            mapper?.let { if (value != null) it.invoke(value) else emptyList() } ?: write<T>(
                value,
                fieldName,
                clazz
            )
        changes[fieldName] = valueToSet
    }

    fun <T> ensureTypes(map: Map<String, KClass<*>>, doc: Map<String, Any?>): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        for (entry in map) {
            write<T>(doc[entry.key], entry.key, entry.value)?.let {
                result[entry.key] = it
            }
        }
        return result
    }

    fun <T, V> addDefaults(list: List<Array<Any>>, doc: MutableMap<String, V>) {
        for (entry in list) {
            val key = entry[0] as String
            val clazz = entry[1] as KClass<*>
            val value = entry[2] as Any
            if (doc[key] == null) {
                write<T>(value, key, clazz)?.let {
                    doc[key] = it as V
                }
            }
        }
    }

    fun <T> read(
        value: Any?,
        fieldName: String,
        clazz: KClass<*>
    ): T? {
        return try {
            val conversion =
                PersistenceConfig.getTypeConversion(clazz) ?: return value as T?
            return conversion.read(value) as T?
        } catch (ex: Exception) {
            PersistenceConfig.onTypeConversionError(
                com.schwarz.crystalapi.TypeConversionErrorWrapper(
                    ex,
                    fieldName, value, clazz
                )
            )
            null
        }
    }

    fun <T> write(
        value: Any?,
        fieldName: String,
        clazz: KClass<*>
    ): T? {
        return try {
            val conversion =
                PersistenceConfig.getTypeConversion(clazz) ?: return value as T?
            return conversion.write(value) as T?
        } catch (ex: Exception) {
            PersistenceConfig.onTypeConversionError(
                com.schwarz.crystalapi.TypeConversionErrorWrapper(
                    ex,
                    fieldName, value, clazz
                )
            )
            null
        }
    }
}
