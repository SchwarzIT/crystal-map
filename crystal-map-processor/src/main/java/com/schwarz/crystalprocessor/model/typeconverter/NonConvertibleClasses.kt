package com.schwarz.crystalprocessor.model.typeconverter

import com.squareup.kotlinpoet.asTypeName

val nonConvertibleClasses = listOf(
    String::class,
    Boolean::class,
    Int::class,
    Long::class,
    Map::class,
    Double::class
)

val nonConvertibleClassesTypeNames = nonConvertibleClasses.map { it.asTypeName() }
