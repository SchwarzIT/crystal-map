package com.schwarz.crystalapi

import kotlin.reflect.KClass

data class TypeConversionErrorWrapper(
    val exception: Exception,
    val fieldName: String,
    val value: Any?,
    val `class`: KClass<*>
)
