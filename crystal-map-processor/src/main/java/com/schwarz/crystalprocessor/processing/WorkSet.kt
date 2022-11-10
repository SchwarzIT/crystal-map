package com.schwarz.crystalprocessor.processing

import com.schwarz.crystalprocessor.Logger
import javax.annotation.processing.ProcessingEnvironment

interface WorkSet {

    fun preValidate(logger: Logger)

    fun loadModels(logger: Logger, env: ProcessingEnvironment)
}
