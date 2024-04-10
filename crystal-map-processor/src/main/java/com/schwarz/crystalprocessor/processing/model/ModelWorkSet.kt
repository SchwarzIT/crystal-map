package com.schwarz.crystalprocessor.processing.model

import com.schwarz.crystalprocessor.Logger
import com.schwarz.crystalprocessor.model.EntityFactory
import com.schwarz.crystalprocessor.model.entity.BaseModelHolder
import com.schwarz.crystalprocessor.model.entity.EntityHolder
import com.schwarz.crystalprocessor.model.entity.SchemaClassHolder
import com.schwarz.crystalprocessor.model.entity.WrapperEntityHolder
import com.schwarz.crystalprocessor.model.source.ReducedSourceModel
import com.schwarz.crystalprocessor.model.source.SourceModel
import com.schwarz.crystalprocessor.processing.WorkSet
import com.schwarz.crystalprocessor.validation.model.ModelValidation
import com.schwarz.crystalprocessor.validation.model.PreModelValidation
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

class ModelWorkSet(
    val allEntityElements: Set<Element>,
    val allWrapperElements: Set<Element>,
    val allSchemaClassElements: Set<Element>,
    val allBaseModelElements: Set<Element>
) :
    WorkSet {

    private val entityModels: MutableMap<String, EntityHolder> = HashMap()

    private val wrapperModels: MutableMap<String, WrapperEntityHolder> = HashMap()

    private val schemaModels: MutableMap<String, SchemaClassHolder> = HashMap()

    private val baseModels: MutableMap<String, BaseModelHolder> = HashMap()

    override fun preValidate(logger: Logger) {
        for (element in hashSetOf(*allBaseModelElements.toTypedArray(), *allEntityElements.toTypedArray(), *allWrapperElements.toTypedArray())) {
            PreModelValidation.validate(element, logger)
        }
    }

    override fun loadModels(logger: Logger, env: ProcessingEnvironment) {
        val allWrapperPaths = allWrapperElements.map { element -> element.toString() }

        for (element in allBaseModelElements) {
            val baseModel = EntityFactory.createBaseModelHolder(SourceModel(element), allWrapperPaths)
            baseModels[element.toString()] = baseModel
        }

        // we can resolve the based on chain when all base models are parsed.
        for (baseModel in baseModels.values) {
            EntityFactory.addBasedOn(baseModel.sourceElement!!, baseModels, baseModel)
        }

        for (element in allEntityElements) {
            val entityModel = EntityFactory.createEntityHolder(SourceModel(element), allWrapperPaths, baseModels)
            entityModels[element.toString()] = entityModel

            entityModel.reducesModels.forEach {
                val reduced = EntityFactory.createEntityHolder(ReducedSourceModel(entityModel.sourceElement, it), allWrapperPaths, baseModels)
                reduced.isReduced = true
                entityModels[reduced.entitySimpleName] = reduced
            }
        }

        for (element in allWrapperElements) {
            val wrapperModel = EntityFactory.createChildEntityHolder(SourceModel(element), allWrapperPaths, baseModels)
            wrapperModels[element.toString()] = wrapperModel

            wrapperModel.reducesModels.forEach {
                val reduced = EntityFactory.createEntityHolder(ReducedSourceModel(wrapperModel.sourceElement, it), allWrapperPaths, baseModels)
                reduced.isReduced = true
                entityModels[reduced.entitySimpleName] = reduced
            }
        }

        val allSchemaClassPaths = allSchemaClassElements.map { element -> element.toString() }
        for (element in allSchemaClassElements) {
            val schemaModel = EntityFactory.createSchemaEntityHolder(SourceModel(element), allSchemaClassPaths, baseModels)
            schemaModels[element.toString()] = schemaModel
        }

        ModelValidation(logger, baseModels, wrapperModels, entityModels).postValidate()
    }

    val entities: List<EntityHolder>
        get() = entityModels.values.toList()

    val wrappers: List<WrapperEntityHolder>
        get() = wrapperModels.values.toList()

    val schemas: List<SchemaClassHolder>
        get() = schemaModels.values.toList()

    val schemaClassPaths: List<String>
        get() = schemaModels.keys.toList()

    val bases: List<BaseModelHolder>
        get() = baseModels.values.toList()
}
