package com.schwarz.crystalcore

import com.schwarz.crystalcore.model.accessor.CblGenerateAccessorHolder
import com.squareup.kotlinpoet.FileSpec

interface ICodeGenerator {

    fun generate(entityToGenerate: FileSpec, settings: ISettings)

    fun generateAndFixAccessors(
        entityToGenerate: FileSpec,
        generateAccessors: MutableList<CblGenerateAccessorHolder>,
        settings: ISettings
    )

}