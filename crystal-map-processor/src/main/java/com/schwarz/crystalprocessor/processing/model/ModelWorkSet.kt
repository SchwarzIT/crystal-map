package com.schwarz.crystalprocessor.processing.model

import com.schwarz.crystalprocessor.Logger
import com.schwarz.crystalprocessor.model.EntityFactory
import com.schwarz.crystalprocessor.model.entity.BaseModelHolder
import com.schwarz.crystalprocessor.model.entity.EntityHolder
import com.schwarz.crystalprocessor.model.entity.SchemaClassHolder
import com.schwarz.crystalprocessor.model.entity.WrapperEntityHolder
import com.schwarz.crystalprocessor.model.source.ReducedSourceModel
import com.schwarz.crystalprocessor.model.source.SourceModel
import com.schwarz.crystalprocessor.model.typeconverter.ImportedTypeConverterHolder
import com.schwarz.crystalprocessor.model.typeconverter.TypeConverterExporterHolder
import com.schwarz.crystalprocessor.model.typeconverter.TypeConverterHolder
import com.schwarz.crystalprocessor.model.typeconverter.TypeConverterHolderFactory
import com.schwarz.crystalprocessor.processing.WorkSet
import com.schwarz.crystalprocessor.validation.model.ModelValidation
import com.schwarz.crystalprocessor.validation.model.PreModelValidation
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

class ModelWorkSet(
    val allEntityElements: Set<Element>,
    val allWrapperElements: Set<Element>,
    val allSchemaClassElements: Set<Element>,
    val allBaseModelElements: Set<Element>,
    val allTypeConverterElements: Set<Element>,
    val allTypeConverterExporterElements: Set<Element>,
    val allTypeConverterImporterElements: Set<Element>
) :
    WorkSet {

    private val entityModels: MutableMap<String, EntityHolder> = HashMap()

    private val wrapperModels: MutableMap<String, WrapperEntityHolder> = HashMap()

    private val schemaModels: MutableMap<String, SchemaClassHolder> = HashMap()

    private val wrapperBaseModels: MutableMap<String, BaseModelHolder> = HashMap()

    private val schemaBaseModels: MutableMap<String, BaseModelHolder> = HashMap()

    private val typeConverterModels: MutableMap<String, TypeConverterHolder> = HashMap()

    private val typeConverterExporterModels: MutableMap<String, TypeConverterExporterHolder> =
        HashMap()

    private val importedTypeConverterModels: MutableList<ImportedTypeConverterHolder> =
        mutableListOf()

    override fun preValidate(logger: Logger) {
        for (element in hashSetOf(
            *allBaseModelElements.toTypedArray(),
            *allEntityElements.toTypedArray(),
            *allWrapperElements.toTypedArray()
        )) {
            PreModelValidation.validate(element, logger)
        }
        allTypeConverterElements.forEach {
            PreModelValidation.validateTypeConverter(it, logger)
        }
        allTypeConverterExporterElements.forEach {
            PreModelValidation.validateTypeConverterExporter(it, logger)
        }
        allTypeConverterImporterElements.forEach {
            PreModelValidation.validateTypeConverterImporter(it, logger)
        }
    }

    override fun loadModels(logger: Logger, env: ProcessingEnvironment) {
        val allWrapperPaths = allWrapperElements.map { element -> element.toString() }
        val allSchemaClassPaths = allSchemaClassElements.map { element -> element.toString() }

        for (element in allBaseModelElements) {
            val wrapperBaseModel = EntityFactory.createWrapperBaseModelHolder(SourceModel(element), allWrapperPaths)
            wrapperBaseModels[element.toString()] = wrapperBaseModel

            val schemaBaseModel = EntityFactory.createSchemaBaseModelHolder(SourceModel(element), allSchemaClassPaths)
            schemaBaseModels[element.toString()] = schemaBaseModel
        }

        // we can resolve the based on chain when all base models are parsed.
        for (baseModel in wrapperBaseModels.values) {
            EntityFactory.addBasedOn(baseModel.sourceElement, wrapperBaseModels, baseModel)
        }

        for (baseModel in schemaBaseModels.values) {
            EntityFactory.addBasedOn(baseModel.sourceElement, schemaBaseModels, baseModel)
        }

        for (element in allEntityElements) {
            val entityModel = EntityFactory.createEntityHolder(SourceModel(element), allWrapperPaths, wrapperBaseModels)
            entityModels[element.toString()] = entityModel

            entityModel.reducesModels.forEach {
                val reduced = EntityFactory.createEntityHolder(
                    ReducedSourceModel(
                        entityModel.sourceElement,
                        it
                    ),
                    allWrapperPaths,
                    wrapperBaseModels
                )
                reduced.isReduced = true
                entityModels[reduced.entitySimpleName] = reduced
            }
        }

        for (element in allWrapperElements) {
            val wrapperModel = EntityFactory.createChildEntityHolder(
                SourceModel(element),
                allWrapperPaths,
                wrapperBaseModels
            )
            wrapperModels[element.toString()] = wrapperModel

            wrapperModel.reducesModels.forEach {
                val reduced = EntityFactory.createEntityHolder(
                    ReducedSourceModel(
                        wrapperModel.sourceElement,
                        it
                    ),
                    allWrapperPaths,
                    wrapperBaseModels
                )
                reduced.isReduced = true
                entityModels[reduced.entitySimpleName] = reduced
            }
        }

        for (element in allSchemaClassElements) {
            val schemaModel = EntityFactory.createSchemaEntityHolder(
                SourceModel(element),
                allSchemaClassPaths,
                schemaBaseModels
            )
            schemaModels[element.toString()] = schemaModel
        }

        allTypeConverterImporterElements.forEach { element ->
            importedTypeConverterModels.addAll(TypeConverterHolderFactory.importedTypeConverterHolders(element))
        }

        allTypeConverterElements.forEach {
            val typeConverterHolder = TypeConverterHolderFactory.typeConverterHolder(it)
            typeConverterModels[it.toString()] = typeConverterHolder
        }

        allTypeConverterExporterElements.forEach {
            val typeConverterExporterHolder = TypeConverterExporterHolder(it)
            typeConverterExporterModels[it.toString()] = typeConverterExporterHolder
        }

        ModelValidation(
            logger,
            wrapperModels,
            entityModels,
            typeConverterModels.values.toList(),
            importedTypeConverterModels
        ).postValidate()
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
        get() = wrapperBaseModels.values.toList()

    val typeConverters: List<TypeConverterHolder>
        get() = typeConverterModels.values.toList()

    val importedTypeConverters: List<ImportedTypeConverterHolder> get() = importedTypeConverterModels

    val typeConverterExporters: List<TypeConverterExporterHolder>
        get() = typeConverterExporterModels.values.toList()
}
