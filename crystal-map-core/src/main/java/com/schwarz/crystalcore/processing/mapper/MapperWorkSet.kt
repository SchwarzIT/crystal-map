package com.schwarz.crystalcore.processing.mapper

import com.schwarz.crystalcore.ILogger
import com.schwarz.crystalcore.model.MapperFactory
import com.schwarz.crystalcore.model.mapper.MapperHolder
import com.schwarz.crystalcore.model.source.ISourceMapperModel
import com.schwarz.crystalcore.processing.WorkSet
import com.schwarz.crystalcore.validation.mapper.MapperValidation

class MapperWorkSet<T>(
    val allMapperElements: Set<ISourceMapperModel<T>>,
    private val preValidate: (ISourceMapperModel<T>, ILogger<T>) -> Unit,
) : WorkSet<T> {
    private val mapperModels: MutableMap<String, MapperHolder<T>> = HashMap()

    override fun preValidate(logger: ILogger<T>) {
        for (element in allMapperElements) {
            preValidate(element, logger)
        }
    }

    override fun loadModels(logger: ILogger<T>) {
        for (element in allMapperElements) {
            val baseModel = MapperFactory.create(element)
            mapperModels[element.toString()] = baseModel
        }

        MapperValidation(logger, mapperModels).postValidate()
    }

    val mappers: List<MapperHolder<T>>
        get() = mapperModels.values.toList()
}
