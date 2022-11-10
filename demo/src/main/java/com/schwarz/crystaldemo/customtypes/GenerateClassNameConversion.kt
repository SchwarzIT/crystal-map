package com.schwarz.crystaldemo.customtypes

import com.schwarz.crystalapi.TypeConversion

class GenerateClassNameConversion : TypeConversion {

    override fun write(value: Any?): Any? {
        return when (value) {
            is GenerateClassName -> {
                value.toString()
            }
            else -> value
        }
    }

    override fun read(value: Any?): GenerateClassName =
        when (value) {
            is String -> {
                GenerateClassName(value)
            }
            else -> GenerateClassName()
        }
}
