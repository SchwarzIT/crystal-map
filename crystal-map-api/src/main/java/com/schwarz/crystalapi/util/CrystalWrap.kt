@file:Suppress("UNCHECKED_CAST")

package com.schwarz.crystalapi.util

import com.schwarz.crystalapi.ITypeConverter
import com.schwarz.crystalapi.PersistenceConfig

object CrystalWrap {

    inline fun <reified T> get(
        changes: MutableMap<String, Any?>,
        doc: MutableMap<String, out Any?>,
        fieldName: String,
        mapper: ((MutableMap<String, Any?>?) -> T?)
    ): T? = (changes[fieldName] ?: doc[fieldName])?.let { value ->
        catchTypeConversionError(fieldName, value) {
            mapper.invoke(value as MutableMap<String, Any?>)
        }
    }

    inline fun <reified T, reified U> get(
        changes: MutableMap<String, Any?>,
        doc: MutableMap<String, out Any?>,
        fieldName: String,
        typeConverter: ITypeConverter<T, U>
    ): T? = (changes[fieldName] ?: doc[fieldName])?.let { value ->
        catchTypeConversionError(fieldName, value) {
            typeConverter.read(value as U)
        }
    }

    inline fun <reified T> get(
        changes: MutableMap<String, Any?>,
        doc: MutableMap<String, out Any?>,
        fieldName: String
    ): T? = (changes[fieldName] ?: doc[fieldName])?.let { value ->
        catchTypeConversionError(fieldName, value) {
            value as T
        }
    }

    inline fun <T> getList(
        changes: MutableMap<String, Any?>,
        doc: MutableMap<String, out Any?>,
        fieldName: String,
        mapper: ((List<MutableMap<String, Any?>>?) -> List<T>)
    ): List<T>? = (changes[fieldName] ?: doc[fieldName])?.let { value ->
        catchTypeConversionError(fieldName, value) {
            mapper.invoke(value as List<MutableMap<String, Any?>>)
        }
    }

    inline fun <T, reified U> getList(
        changes: MutableMap<String, Any?>,
        doc: MutableMap<String, out Any?>,
        fieldName: String,
        typeConverter: ITypeConverter<T, U>
    ): List<T>? = (changes[fieldName] ?: doc[fieldName])?.let { value ->
        catchTypeConversionError(fieldName, value) {
            ((value as List<Any>).map { it as U }).mapNotNull {
                typeConverter.read(it)
            }
        }
    }

    inline fun <reified T> getList(
        changes: MutableMap<String, Any?>,
        doc: MutableMap<String, out Any?>,
        fieldName: String
    ): List<T>? = (changes[fieldName] ?: doc[fieldName])?.let { value ->
        catchTypeConversionError(fieldName, value) {
            (value as List<Any>).map { it as T }
        }
    }

    inline fun <T> set(
        changes: MutableMap<String, Any?>,
        fieldName: String,
        value: T,
        mapper: ((T) -> MutableMap<String, Any>)
    ) {
        changes[fieldName] = mapper.invoke(value)
    }

    inline fun <T> set(
        changes: MutableMap<String, Any?>,
        fieldName: String,
        value: T?,
        typeConverter: ITypeConverter<T, *>
    ) {
        changes[fieldName] = typeConverter.write(value)
    }

    inline fun <T> set(
        changes: MutableMap<String, Any?>,
        fieldName: String,
        value: T?
    ) {
        changes[fieldName] = value
    }

    inline fun <T> setList(
        changes: MutableMap<String, Any?>,
        fieldName: String,
        value: List<T>?,
        mapper: ((List<T>) -> List<MutableMap<String, Any>>)
    ) {
        changes[fieldName] = if (value != null) mapper.invoke(value) else emptyList()
    }

    inline fun <T> setList(
        changes: MutableMap<String, Any?>,
        fieldName: String,
        value: List<T>?,
        typeConverter: ITypeConverter<T, *>
    ) {
        changes[fieldName] = value?.map { typeConverter.write(it) }
    }

    inline fun <T> setList(
        changes: MutableMap<String, Any?>,
        fieldName: String,
        value: List<T>?
    ) {
        changes[fieldName] = value
    }

    fun validate(
        doc: MutableMap<String, Any>,
        mandatoryFields: Array<String>
    ) {
        for (mandatoryField in mandatoryFields) {
            doc[mandatoryField]!!
        }
    }

    inline fun <reified T> catchTypeConversionError(fieldName: String, value: Any, task: () -> T): T? = try {
        task()
    } catch (cce: ClassCastException) {
        PersistenceConfig.onTypeConversionError(
            com.schwarz.crystalapi.TypeConversionErrorWrapper(
                cce,
                fieldName,
                value,
                T::class
            )
        )
        null
    }
}
