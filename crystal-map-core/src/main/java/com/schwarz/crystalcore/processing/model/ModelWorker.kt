package com.schwarz.crystalcore.processing.model

import com.schwarz.crystalcore.ICodeGenerator
import com.schwarz.crystalcore.ILogger
import com.schwarz.crystalcore.ISettings
import com.schwarz.crystalcore.documentation.DocumentationGenerator
import com.schwarz.crystalcore.documentation.EntityRelationshipGenerator
import com.schwarz.crystalcore.generation.model.CommonInterfaceGeneration
import com.schwarz.crystalcore.generation.model.EntityGeneration
import com.schwarz.crystalcore.generation.model.SchemaGeneration
import com.schwarz.crystalcore.generation.model.TypeConverterExporterObjectGeneration
import com.schwarz.crystalcore.generation.model.TypeConverterObjectGeneration
import com.schwarz.crystalcore.generation.model.WrapperGeneration
import com.schwarz.crystalcore.meta.SchemaGenerator
import com.schwarz.crystalcore.model.entity.BaseEntityHolder
import com.schwarz.crystalcore.model.entity.SchemaClassHolder
import com.schwarz.crystalcore.model.field.CblFieldHolder
import com.schwarz.crystalcore.model.typeconverter.TypeConverterHolder
import com.schwarz.crystalcore.model.typeconverter.TypeConverterHolderForEntityGeneration
import com.schwarz.crystalcore.processing.Worker
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeName

class ModelWorker<T>(
    override val logger: ILogger<T>,
    override val codeGenerator: ICodeGenerator,
    override val settings: ISettings,
    override val workSet: ModelWorkSet<T>,
    // True when the work set may only cover a subset of the entities
    // (incremental KSP processing): the doc/schema side outputs then merge with
    // the previous run instead of being rebuilt from the partial model.
    private val mergeSideOutputs: Boolean = false,
    // Source files that were (re)processed in this run; previously generated
    // side-output entries originating from them are purged when the run no
    // longer produces them.
    private val reprocessedFilePaths: Set<String> = emptySet(),
    // Extracts a stable file path from a platform originating-file object
    // (KSFile for KSP); null when the platform cannot provide one (kapt).
    private val originFilePath: (Any) -> String? = { null },
) : Worker<ModelWorkSet<T>, T> {
    private var documentationGenerator: DocumentationGenerator? = null
    private var entityRelationshipGenerator: EntityRelationshipGenerator? = null
    private var schemaGenerator: SchemaGenerator? = null

    override fun init() {
        settings.documentationPath?.let {
            documentationGenerator =
                DocumentationGenerator(it, settings.documentationFilename ?: "default.html")
        }
        settings.schemaPath?.let {
            schemaGenerator =
                SchemaGenerator(
                    it,
                    settings.schemaFilename ?: "schema.json",
                )
        }

        settings.entityRelationshipPath?.let {
            entityRelationshipGenerator =
                EntityRelationshipGenerator(it, settings.entityRelationshipFilename ?: "default.gv")
        }
    }

    override fun doWork(
        workSet: ModelWorkSet<T>,
        useSuspend: Boolean,
    ) {
        val typeConvertersByConvertedClass: Map<TypeName, TypeConverterHolderForEntityGeneration> =
            (workSet.typeConverters + workSet.importedTypeConverters).associateBy {
                it.domainClassTypeName
            }

        workSet.typeConverters.forEach {
            codeGenerator.generate(
                TypeConverterObjectGeneration.generateTypeConverterObject(it),
                settings,
                it.originatingFiles,
                aggregating = false,
            )
        }

        workSet.typeConverterExporters.forEach {
            val exporterOrigins = it.originatingFiles +
                workSet.typeConverters.flatMap { tc -> tc.originatingFiles }
            codeGenerator.generate(
                TypeConverterExporterObjectGeneration.generateTypeConverterExporterObject(
                    it,
                    workSet.typeConverters,
                ),
                settings,
                exporterOrigins,
                aggregating = false,
            )
        }

        val generatedInterfaces = mutableSetOf<String>()

        for (baseModelHolder in workSet.bases) {
            generateInterface(
                generatedInterfaces,
                baseModelHolder,
                useSuspend,
                typeConvertersByConvertedClass,
            )
        }

        processAndFixAccessors(
            workSet.entities,
            generatedInterfaces,
            useSuspend,
            typeConvertersByConvertedClass,
        ) {
            EntityGeneration().generateModel(it, useSuspend, typeConvertersByConvertedClass)
        }

        processAndFixAccessors(
            workSet.wrappers,
            generatedInterfaces,
            useSuspend,
            typeConvertersByConvertedClass,
        ) {
            WrapperGeneration().generateModel(it, useSuspend, typeConvertersByConvertedClass)
        }

        process(workSet.schemas) {
            SchemaGeneration().generateModel(
                it,
                workSet.schemaClassPaths,
                typeConvertersByConvertedClass,
            )
        }

        documentationGenerator?.generate(mergeSideOutputs, reprocessedFilePaths)
        entityRelationshipGenerator?.generate(mergeSideOutputs, reprocessedFilePaths)
        schemaGenerator?.generate(mergeSideOutputs, reprocessedFilePaths)
    }

    private fun process(
        schemaModels: List<SchemaClassHolder<T>>,
        generate: (SchemaClassHolder<T>) -> FileSpec,
    ) {
        for (model in schemaModels) {
            val sourcePaths = sourcePathsOf(model)
            documentationGenerator?.addEntitySegments(model, sourcePaths)
            schemaGenerator?.addEntity(model, sourcePaths)
            entityRelationshipGenerator?.addEntityNodes(model, sourcePaths)
            generate(model).apply {
                codeGenerator.generate(this, settings, model.allOriginatingFiles, aggregating = false)
            }
        }
    }

    private fun <E : BaseEntityHolder<T>> processAndFixAccessors(
        models: List<E>,
        generatedInterfaces: MutableSet<String>,
        useSuspend: Boolean,
        typeConvertersByConvertedClass: Map<TypeName, TypeConverterHolderForEntityGeneration>,
        generate: (E) -> FileSpec,
    ) {
        for (model in models) {
            generateInterface(
                generatedInterfaces,
                model,
                useSuspend,
                typeConvertersByConvertedClass,
            )
            val sourcePaths = sourcePathsOf(model)
            documentationGenerator?.addEntitySegments(model, sourcePaths)
            schemaGenerator?.addEntity(model, sourcePaths)
            entityRelationshipGenerator?.addEntityNodes(model, sourcePaths)
            val tcOrigins = usedTypeConverterOrigins(model, typeConvertersByConvertedClass)
            val origins = model.allOriginatingFiles + tcOrigins
            generate(model).apply {
                if (model.generateAccessors.isNotEmpty()) {
                    codeGenerator.generateAndFixAccessors(
                        this,
                        model.generateAccessors,
                        settings,
                        origins,
                        aggregating = false,
                    )
                } else {
                    codeGenerator.generate(
                        this,
                        settings,
                        origins,
                        aggregating = false,
                    )
                }
            }
        }
    }

    private fun generateInterface(
        generatedInterfaces: MutableSet<String>,
        holder: BaseEntityHolder<T>,
        useSuspend: Boolean,
        typeConvertersByConvertedClass: Map<TypeName, TypeConverterHolderForEntityGeneration>,
    ) {
        if (generatedInterfaces
                .contains(
                    "${holder.sourcePackage}.${holder.sourceClazzSimpleName}",
                ).not()
        ) {
            codeGenerator.generate(
                CommonInterfaceGeneration().generateModel(
                    holder,
                    useSuspend,
                    typeConvertersByConvertedClass,
                ),
                settings,
                holder.allOriginatingFiles,
                aggregating = false,
            )
            generatedInterfaces.add("${holder.sourcePackage}.${holder.sourceClazzSimpleName}")
        }
    }

    private fun sourcePathsOf(holder: BaseEntityHolder<T>): List<String> = holder.allOriginatingFiles.mapNotNull(originFilePath)

    private fun usedTypeConverterOrigins(
        holder: BaseEntityHolder<T>,
        typeConvertersByConvertedClass: Map<TypeName, TypeConverterHolderForEntityGeneration>,
    ): List<Any> =
        holder.allFields
            .filterIsInstance<CblFieldHolder>()
            .filter { !it.isNonConvertibleClass && !it.isTypeOfSubEntity }
            .mapNotNull { typeConvertersByConvertedClass[it.fieldType] }
            .filterIsInstance<TypeConverterHolder>()
            .flatMap { it.originatingFiles }
            .distinct()
}
