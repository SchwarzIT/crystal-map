package com.kaufland.processing.mapper

import com.kaufland.Logger
import com.kaufland.generation.CodeGenerator
import com.kaufland.generation.mapper.MapperGeneration
import com.kaufland.processing.Worker
import kaufland.com.coachbasebinderapi.mapify.Mapper
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment

class MapperWorker(override val logger: Logger, override val codeGenerator: CodeGenerator, override val processingEnv: ProcessingEnvironment) : Worker<MapperWorkSet> {

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
