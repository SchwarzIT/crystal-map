package com.kaufland.processing.model

import com.kaufland.Logger
import com.kaufland.model.EntityFactory
import com.kaufland.model.entity.BaseModelHolder
import com.kaufland.model.entity.EntityHolder
import com.kaufland.model.entity.WrapperEntityHolder
import com.kaufland.processing.WorkSet
import com.kaufland.validation.model.ModelValidation
import com.kaufland.validation.model.PreModelValidation
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

class ModelWorkSet(val allEntityElements: Set<Element>, val allWrapperElements: Set<Element>, val allBaseModelElements: Set<Element>) : WorkSet {

    private val entityModels: MutableMap<String, EntityHolder> = HashMap()

    private val wrapperModels: MutableMap<String, WrapperEntityHolder> = HashMap()

    private val baseModels: MutableMap<String, BaseModelHolder> = HashMap()

    override fun preValidate(logger: Logger) {
        for (element in hashSetOf(*allBaseModelElements.toTypedArray(), *allEntityElements.toTypedArray(), *allWrapperElements.toTypedArray())) {
            PreModelValidation.validate(element, logger)
        }
    }

    override fun loadModels(logger: Logger, env: ProcessingEnvironment) {

        val allWrapperStrings = allWrapperElements.map { element -> element.toString() }

        for (element in allBaseModelElements) {
            val baseModel = EntityFactory.createBaseModelHolder(element, allWrapperStrings)
            baseModels[element.toString()] = baseModel
        }

        for (element in allEntityElements) {
            val entityModel = EntityFactory.createEntityHolder(element, allWrapperStrings, baseModels)
            entityModels[element.toString()] = entityModel
        }

        for (element in allWrapperElements) {
            val wrapperModel = EntityFactory.createChildEntityHolder(element, allWrapperStrings, baseModels)
            wrapperModels[element.toString()] = wrapperModel
        }

        ModelValidation(logger, baseModels, wrapperModels, entityModels).postValidate()
    }

    val entities: List<EntityHolder>
        get() = entityModels.values.toList()

    val wrappers: List<WrapperEntityHolder>
        get() = wrapperModels.values.toList()

    val bases: List<BaseModelHolder>
        get() = baseModels.values.toList()





}