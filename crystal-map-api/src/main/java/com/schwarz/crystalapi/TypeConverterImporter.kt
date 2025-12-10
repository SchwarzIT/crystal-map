package com.schwarz.crystalapi

import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class TypeConverterImporter(
    val typeConverterExporter: KClass<out ITypeConverterExporter>,
)
