package com.schwarz.crystalcore.processing.mapper

import com.schwarz.crystalcore.ICodeGenerator
import com.schwarz.crystalcore.ILogger
import com.schwarz.crystalcore.ISettings
import com.schwarz.crystalcore.generation.mapper.MapperGeneration
import com.schwarz.crystalcore.processing.Worker

class MapperWorker<T>(
    override val logger: ILogger<T>,
    override val codeGenerator: ICodeGenerator,
    override val settings: ISettings,
    override val workSet: MapperWorkSet<T>,
) : Worker<MapperWorkSet<T>, T> {
    val mapperGeneration: MapperGeneration<T> = MapperGeneration<T>()

    override fun init() {
    }

    override fun doWork(
        workSet: MapperWorkSet<T>,
        useSuspend: Boolean,
    ) {
        for (mapper in workSet.mappers) {
            mapperGeneration.generate(mapper).apply {
                codeGenerator.generate(this, settings)
            }
        }
    }
}
