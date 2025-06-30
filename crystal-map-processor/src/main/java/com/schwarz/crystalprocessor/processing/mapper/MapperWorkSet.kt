package com.schwarz.crystalprocessor.processing.mapper

import com.schwarz.crystalcore.ILogger
import com.schwarz.crystalprocessor.model.MapperFactory
import com.schwarz.crystalprocessor.model.mapper.MapperHolder
import com.schwarz.crystalcore.processing.WorkSet
import com.schwarz.crystalprocessor.validation.mapper.MapperValidation
import com.schwarz.crystalprocessor.validation.mapper.PreMapperValidation
import javax.lang.model.element.Element

class MapperWorkSet(val allMapperElements: Set<Element>) : WorkSet<Element> {

    private val mapperModels: MutableMap<String, MapperHolder> = HashMap()

    override fun preValidate(logger: ILogger<Element>) {
        for (element in allMapperElements) {
            PreMapperValidation.validate(element, logger)
        }
    }

    override fun loadModels(logger: ILogger<Element>) {
        for (element in allMapperElements) {
            val baseModel = MapperFactory.create(element)
            mapperModels[element.toString()] = baseModel
        }

        MapperValidation(logger, mapperModels).postValidate()
    }

    val mappers: List<MapperHolder>
        get() = mapperModels.values.toList()
}
