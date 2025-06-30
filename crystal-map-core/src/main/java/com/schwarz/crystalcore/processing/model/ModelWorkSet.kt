package com.schwarz.crystalcore.processing.model

import com.schwarz.crystalcore.ILogger
import com.schwarz.crystalcore.model.EntityFactory
import com.schwarz.crystalcore.model.entity.BaseModelHolder
import com.schwarz.crystalcore.model.entity.EntityHolder
import com.schwarz.crystalcore.model.entity.SchemaClassHolder
import com.schwarz.crystalcore.model.entity.WrapperEntityHolder
import com.schwarz.crystalcore.model.source.ISourceModel
import com.schwarz.crystalcore.validation.model.ModelValidation
import com.schwarz.crystalcore.model.source.ReducedSourceModel
import com.schwarz.crystalcore.model.typeconverter.ImportedTypeConverterHolder
import com.schwarz.crystalcore.model.typeconverter.TypeConverterExporterHolder
import com.schwarz.crystalcore.model.typeconverter.TypeConverterHolder
import com.schwarz.crystalcore.model.typeconverter.TypeConverterHolderFactory
import com.schwarz.crystalcore.processing.WorkSet
import com.schwarz.crystalcore.validation.model.PreModelValidation

class ModelWorkSet<T>(
    val allEntityElements: Set<ISourceModel<T>>,
    val allWrapperElements: Set<ISourceModel<T>>,
    val allSchemaClassElements: Set<ISourceModel<T>>,
    val allBaseModelElements: Set<ISourceModel<T>>,
    val allTypeConverterElements: Set<ISourceModel<T>>,
    val allTypeConverterExporterElements: Set<ISourceModel<T>>,
    val allTypeConverterImporterElements: Set<ISourceModel<T>>
) :
    WorkSet<T> {

    private val entityModels: MutableMap<String, EntityHolder<T>> = HashMap()

    private val wrapperModels: MutableMap<String, WrapperEntityHolder<T>> = HashMap()

    private val schemaModels: MutableMap<String, SchemaClassHolder<T>> = HashMap()

    private val wrapperBaseModels: MutableMap<String, BaseModelHolder<T>> = HashMap()

    private val schemaBaseModels: MutableMap<String, BaseModelHolder<T>> = HashMap()

    private val typeConverterModels: MutableMap<String, TypeConverterHolder> = HashMap()

    private val typeConverterExporterModels: MutableMap<String, TypeConverterExporterHolder<T>> =
        HashMap()

    private val importedTypeConverterModels: MutableList<ImportedTypeConverterHolder> =
        mutableListOf()

    override fun preValidate(logger: ILogger<T>) {
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

    override fun loadModels(logger: ILogger<T>) {
        val allWrapperPaths = allWrapperElements.map { element -> element.fullQualifiedName }
        val allSchemaClassPaths = allSchemaClassElements.map { element -> element.fullQualifiedName }

        for (element in allBaseModelElements) {
            val wrapperBaseModel = EntityFactory.createWrapperBaseModelHolder(element, allWrapperPaths)
            wrapperBaseModels[element.fullQualifiedName] = wrapperBaseModel

            val schemaBaseModel = EntityFactory.createSchemaBaseModelHolder(element, allSchemaClassPaths)
            schemaBaseModels[element.fullQualifiedName] = schemaBaseModel
        }

        // we can resolve the based on chain when all base models are parsed.
        for (baseModel in wrapperBaseModels.values) {
            EntityFactory.addBasedOn(baseModel.sourceElement, wrapperBaseModels, baseModel)
        }

        for (baseModel in schemaBaseModels.values) {
            EntityFactory.addBasedOn(baseModel.sourceElement, schemaBaseModels, baseModel)
        }

        for (element in allEntityElements) {
            val entityModel = EntityFactory.createEntityHolder(element, allWrapperPaths, wrapperBaseModels)
            entityModels[element.fullQualifiedName] = entityModel

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
                element,
                allWrapperPaths,
                wrapperBaseModels
            )
            wrapperModels[element.fullQualifiedName] = wrapperModel

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
                element,
                allSchemaClassPaths,
                schemaBaseModels
            )
            schemaModels[element.fullQualifiedName] = schemaModel
        }

        allTypeConverterImporterElements.forEach { element ->
            importedTypeConverterModels.addAll(TypeConverterHolderFactory.importedTypeConverterHolders(element))
        }

        allTypeConverterElements.forEach {
            val typeConverterHolder = TypeConverterHolderFactory.typeConverterHolder(it)
            typeConverterModels[it.fullQualifiedName] = typeConverterHolder
        }

        allTypeConverterExporterElements.forEach {
            val typeConverterExporterHolder = TypeConverterExporterHolder(it)
            typeConverterExporterModels[it.fullQualifiedName] = typeConverterExporterHolder
        }

        ModelValidation(
            logger,
            wrapperModels,
            entityModels,
            typeConverterModels.values.toList(),
            importedTypeConverterModels
        ).postValidate()
    }

    val entities: List<EntityHolder<T>>
        get() = entityModels.values.toList()

    val wrappers: List<WrapperEntityHolder<T>>
        get() = wrapperModels.values.toList()

    val schemas: List<SchemaClassHolder<T>>
        get() = schemaModels.values.toList()

    val schemaClassPaths: List<String>
        get() = schemaModels.keys.toList()

    val bases: List<BaseModelHolder<T>>
        get() = wrapperBaseModels.values.toList()

    val typeConverters: List<TypeConverterHolder>
        get() = typeConverterModels.values.toList()

    val importedTypeConverters: List<ImportedTypeConverterHolder> get() = importedTypeConverterModels

    val typeConverterExporters: List<TypeConverterExporterHolder<T>>
        get() = typeConverterExporterModels.values.toList()
}
