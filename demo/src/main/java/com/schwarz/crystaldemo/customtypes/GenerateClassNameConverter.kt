package com.schwarz.crystaldemo.customtypes

import com.schwarz.crystalapi.ITypeConverter
import com.schwarz.crystalapi.TypeConverter

@TypeConverter
abstract class GenerateClassNameConverter : ITypeConverter<GenerateClassName, String> {
    override fun write(value: GenerateClassName?): String? = value?.toString()

    override fun read(value: String?): GenerateClassName? = value?.let { GenerateClassName(it) }
}
