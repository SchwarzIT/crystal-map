package com.schwarz.crystaldemo.customtypes

import com.schwarz.crystalapi.ITypeConverter
import com.schwarz.crystalapi.TypeConverter

@TypeConverter
abstract class DoubleConverter : ITypeConverter<Double, Number> {
    override fun write(value: Double?): Number? = value

    override fun read(value: Number?): Double? = value?.toDouble()
}
