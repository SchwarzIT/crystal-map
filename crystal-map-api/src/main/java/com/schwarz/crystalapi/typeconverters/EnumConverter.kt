package com.schwarz.crystalapi.typeconverters

import com.schwarz.crystalapi.ITypeConverter
import kotlin.reflect.KClass

class EnumConverter<T : Enum<T>> (private val enumClass: KClass<T>) : ITypeConverter<T, String> {
    override fun write(value: T?): String? =
        value?.toString()

    override fun read(value: String?): T? =
        value?.let { java.lang.Enum.valueOf(enumClass.java, value) }
}
