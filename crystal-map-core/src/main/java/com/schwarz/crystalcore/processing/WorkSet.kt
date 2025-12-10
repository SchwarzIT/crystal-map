package com.schwarz.crystalcore.processing

import com.schwarz.crystalcore.ILogger

interface WorkSet<T> {
    fun preValidate(logger: ILogger<T>)

    fun loadModels(logger: ILogger<T>)
}
