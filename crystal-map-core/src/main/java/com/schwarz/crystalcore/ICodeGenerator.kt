package com.schwarz.crystalcore

import com.schwarz.crystalcore.model.accessor.CblGenerateAccessorHolder
import com.squareup.kotlinpoet.FileSpec

interface ICodeGenerator {
    fun generate(
        entityToGenerate: FileSpec,
        settings: ISettings,
    )

    fun generate(
        entityToGenerate: FileSpec,
        settings: ISettings,
        originatingFiles: List<Any>,
        aggregating: Boolean = true,
    ) {
        generate(entityToGenerate, settings)
    }

    fun generateAndFixAccessors(
        entityToGenerate: FileSpec,
        generateAccessors: MutableList<CblGenerateAccessorHolder>,
        settings: ISettings,
    )

    fun generateAndFixAccessors(
        entityToGenerate: FileSpec,
        generateAccessors: MutableList<CblGenerateAccessorHolder>,
        settings: ISettings,
        originatingFiles: List<Any>,
        aggregating: Boolean = true,
    ) {
        generateAndFixAccessors(entityToGenerate, generateAccessors, settings)
    }
}
