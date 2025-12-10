package com.schwarz.crystalapi.converterexport

import com.schwarz.crystalapi.ITypeConverter
import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
annotation class ExportConverter(val type: KClass<out Any>, val converter: KClass<out ITypeConverter<*, *>>)
