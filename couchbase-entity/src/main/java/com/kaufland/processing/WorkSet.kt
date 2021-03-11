package com.kaufland.processing

import com.kaufland.Logger
import javax.annotation.processing.ProcessingEnvironment

interface WorkSet {

    fun preValidate(logger: Logger)

    fun loadModels(logger: Logger, env: ProcessingEnvironment)

}