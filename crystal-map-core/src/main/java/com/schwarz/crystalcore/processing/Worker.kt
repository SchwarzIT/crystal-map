package com.schwarz.crystalcore.processing


import com.schwarz.crystalcore.ICodeGenerator
import com.schwarz.crystalcore.ILogger
import com.schwarz.crystalcore.ISettings
import javax.annotation.processing.RoundEnvironment

interface Worker<T : WorkSet<E>, E> {

    val logger: ILogger<E>

    val codeGenerator: ICodeGenerator

    val settings: ISettings

    val workSet: T

    fun init()

    fun doWork(workSet: T, useSuspend: Boolean)

    fun invoke(useSuspend: Boolean): Boolean {

        workSet.preValidate(logger)

        if (logger.hasErrors()) {
            return false
        }

        workSet.loadModels(logger)

        if (logger.hasErrors()) {
            return false
        }
        doWork(workSet, useSuspend)
        return true
    }
}
