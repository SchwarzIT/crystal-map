package com.kaufland.processing

import com.kaufland.Logger
import com.kaufland.generation.CodeGenerator
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment

interface Worker<T : WorkSet> {

    val logger: Logger

    val codeGenerator: CodeGenerator

    val processingEnv: ProcessingEnvironment

    fun init()

    fun evaluateWorkSet(roundEnv: RoundEnvironment): T

    fun doWork(workSet: T, useSuspend: Boolean)

    fun invoke(roundEnv: RoundEnvironment, useSuspend: Boolean): Boolean {
        val workSet = evaluateWorkSet(roundEnv)

        workSet.preValidate(logger)

        if (logger.hasErrors()) {
            return false
        }

        workSet.loadModels(logger, processingEnv)

        if (logger.hasErrors()) {
            return false
        }
        doWork(workSet, useSuspend)
        return true
    }
}
