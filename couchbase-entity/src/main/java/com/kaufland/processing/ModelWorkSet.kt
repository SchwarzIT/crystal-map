package com.kaufland.processing

import com.kaufland.Logger
import com.kaufland.model.EntityFactory
import com.kaufland.model.entity.BaseModelHolder
import com.kaufland.model.entity.EntityHolder
import com.kaufland.model.entity.WrapperEntityHolder
import com.kaufland.validation.ModelValidation
import com.kaufland.validation.PreModelValidation
import javax.lang.model.element.Element

class ModelWorkSet(val logger: Logger, val allEntityElements: Set<Element>, val allWrapperElements: Set<Element>, val allBaseModelElements: Set<Element>) {

    private val entityModels: MutableMap<String, EntityHolder> = HashMap()

    private val wrapperModels: MutableMap<String, WrapperEntityHolder> = HashMap()

    private val baseModels: MutableMap<String, BaseModelHolder> = HashMap()

    init {
        for (element in hashSetOf(*allBaseModelElements.toTypedArray(), *allEntityElements.toTypedArray(), *allWrapperElements.toTypedArray())) {
            PreModelValidation.validate(element, logger)
        }
    }

    fun loadModels() {

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