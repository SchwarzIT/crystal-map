package com.schwarz.crystaldemo.customtypes

import com.schwarz.crystalapi.ITypeConverter
import com.schwarz.crystalapi.TypeConverter

@TypeConverter
abstract class IntConverter : ITypeConverter<Int, Number> {
    override fun write(value: Int?): Number? = value
    override fun read(value: Number?): Int? = value?.toInt()
}
