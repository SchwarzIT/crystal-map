package com.schwarz.crystalprocessor.processing.mapper

import com.schwarz.crystalprocessor.Logger
import com.schwarz.crystalprocessor.model.MapperFactory
import com.schwarz.crystalprocessor.model.mapper.MapperHolder
import com.schwarz.crystalprocessor.processing.WorkSet
import com.schwarz.crystalprocessor.validation.mapper.MapperValidation
import com.schwarz.crystalprocessor.validation.mapper.PreMapperValidation
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

class MapperWorkSet(val allMapperElements: Set<Element>) : WorkSet {

    private val mapperModels: MutableMap<String, MapperHolder> = HashMap()

    override fun preValidate(logger: Logger) {
        for (element in allMapperElements) {
            PreMapperValidation.validate(element, logger)
        }
    }

    override fun loadModels(logger: Logger, env: ProcessingEnvironment) {

        for (element in allMapperElements) {
            val baseModel = MapperFactory.create(env, element)
            mapperModels[element.toString()] = baseModel
        }

        MapperValidation(logger, mapperModels).postValidate()
    }

    val mappers: List<MapperHolder>
        get() = mapperModels.values.toList()
}
