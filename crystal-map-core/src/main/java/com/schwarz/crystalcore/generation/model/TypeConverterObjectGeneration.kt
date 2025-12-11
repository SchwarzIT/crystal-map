package com.schwarz.crystalcore.generation.model

import com.schwarz.crystalcore.model.typeconverter.TypeConverterHolder
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec

object TypeConverterObjectGeneration {
    fun generateTypeConverterObject(typeConverterHolder: TypeConverterHolder): FileSpec {
        val typeSpec =
            TypeSpec
                .objectBuilder(typeConverterHolder.instanceClassTypeName)
                .superclass(typeConverterHolder.classTypeName)
                .build()

        return FileSpec.get(typeConverterHolder.instanceClassTypeName.packageName, typeSpec)
    }
}
