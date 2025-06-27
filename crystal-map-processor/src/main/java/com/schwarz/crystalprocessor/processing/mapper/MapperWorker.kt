package com.schwarz.crystalprocessor.processing.mapper

import com.schwarz.crystalprocessor.Logger
import com.schwarz.crystalprocessor.generation.CodeGenerator
import com.schwarz.crystalprocessor.generation.mapper.MapperGeneration
import com.schwarz.crystalcore.processing.Worker
import com.schwarz.crystalapi.mapify.Mapper
import com.schwarz.crystalcore.ISettings
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element

class MapperWorker(override val logger: Logger, override val codeGenerator: CodeGenerator,
                   override val settings: ISettings, override val workSet: MapperWorkSet) :
    Worker<MapperWorkSet, Element> {

    val mapperGeneration: MapperGeneration = MapperGeneration()

    override fun init() {
    }

    override fun doWork(workSet: MapperWorkSet, useSuspend: Boolean) {
        for (mapper in workSet.mappers) {
            mapperGeneration.generate(mapper).apply {
                codeGenerator.generate(this, settings)
            }
        }
    }
}
