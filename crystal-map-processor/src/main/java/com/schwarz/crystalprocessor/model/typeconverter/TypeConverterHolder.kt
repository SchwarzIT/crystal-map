package com.schwarz.crystalprocessor.model.typeconverter

import com.squareup.kotlinpoet.ClassName

interface TypeConverterHolderForEntityGeneration {
    val instanceClassTypeName: ClassName
    val domainClassTypeName: ClassName
    val mapClassTypeName: ClassName
}

data class ImportedTypeConverterHolder(
    override val instanceClassTypeName: ClassName,
    override val domainClassTypeName: ClassName,
    override val mapClassTypeName: ClassName
) : TypeConverterHolderForEntityGeneration

data class TypeConverterHolder(
    val classTypeName: ClassName,
    override val instanceClassTypeName: ClassName,
    override val domainClassTypeName: ClassName,
    override val mapClassTypeName: ClassName
) : TypeConverterHolderForEntityGeneration
