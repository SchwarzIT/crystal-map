package com.schwarz.crystalprocessor.model.typeconverter

import com.schwarz.crystalapi.ClassNameDefinition
import com.squareup.kotlinpoet.ClassName

interface TypeConverterHolderForEntityGeneration {
    val instanceClassTypeName: ClassName
    val domainClassTypeName: ClassName
    val mapClassTypeName: ClassName
    val genericTypeNames: List<ClassNameDefinition>
}

data class ImportedTypeConverterHolder(
    override val instanceClassTypeName: ClassName,
    override val domainClassTypeName: ClassName,
    override val mapClassTypeName: ClassName,
    override val genericTypeNames: List<ClassNameDefinition>
) : TypeConverterHolderForEntityGeneration

data class TypeConverterHolder(
    val classTypeName: ClassName,
    override val instanceClassTypeName: ClassName,
    override val domainClassTypeName: ClassName,
    override val mapClassTypeName: ClassName,
    override val genericTypeNames: List<ClassNameDefinition>
) : TypeConverterHolderForEntityGeneration
