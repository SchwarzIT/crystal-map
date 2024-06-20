package com.schwarz.crystaldemo.customtypes

import com.schwarz.crystalapi.ITypeConverter
import com.schwarz.crystalapi.TypeConverter

@TypeConverter
abstract class LongConverter : ITypeConverter<Long, Number> {
    override fun write(value: Long?): Number? = value

    override fun read(value: Number?): Long? = value?.toLong()
}
