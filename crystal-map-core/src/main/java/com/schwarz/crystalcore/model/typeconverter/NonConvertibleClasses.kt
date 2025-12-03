package com.schwarz.crystalcore.model.typeconverter

import com.squareup.kotlinpoet.asTypeName

val nonConvertibleClasses =
    listOf(
        String::class,
        Boolean::class,
        Number::class,
        Map::class,
        Any::class
    )

val nonConvertibleClassesTypeNames = nonConvertibleClasses.map { it.asTypeName() }
