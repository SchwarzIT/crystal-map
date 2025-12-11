package com.schwarz.crystalcore.model.source

import com.schwarz.crystalapi.ClassNameDefinition
import com.squareup.kotlinpoet.ClassName

data class TypeConverterInterface(
    val domainClassTypeName: ClassName,
    val mapClassTypeName: ClassName,
    val genericTypeNames: List<ClassNameDefinition>,
)
