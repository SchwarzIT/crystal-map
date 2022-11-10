package com.schwarz.crystalprocessor.processing.mapper

import com.schwarz.crystalprocessor.Logger
import com.schwarz.crystalprocessor.generation.CodeGenerator
import com.schwarz.crystalprocessor.generation.mapper.MapperGeneration
import com.schwarz.crystalprocessor.processing.Worker
import com.schwarz.crystalapi.mapify.Mapper
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment

class MapperWorker(override val logger: Logger, override val codeGenerator: CodeGenerator, override val processingEnv: ProcessingEnvironment) :
    Worker<MapperWorkSet> {

    val mapperGeneration: MapperGeneration = MapperGeneration()

    override fun init() {
    }

    override fun doWork(workSet: MapperWorkSet, useSuspend: Boolean) {
        for (mapper in workSet.mappers) {
            mapperGeneration.generate(mapper).apply {
                codeGenerator.generate(this, processingEnv)
            }
        }
    }

    override fun evaluateWorkSet(roundEnv: RoundEnvironment): MapperWorkSet = MapperWorkSet(
        allMapperElements = roundEnv.getElementsAnnotatedWith(Mapper::class.java)
    )
}
