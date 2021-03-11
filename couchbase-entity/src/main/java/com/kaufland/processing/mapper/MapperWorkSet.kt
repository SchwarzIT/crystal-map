package com.kaufland.processing.mapper

import com.kaufland.Logger
import com.kaufland.model.EntityFactory
import com.kaufland.model.MapperFactory
import com.kaufland.model.mapper.MapperHolder
import com.kaufland.processing.WorkSet
import com.kaufland.validation.mapper.MapperValidation
import com.kaufland.validation.mapper.PreMapperValidation
import com.kaufland.validation.model.ModelValidation
import com.kaufland.validation.model.PreModelValidation
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