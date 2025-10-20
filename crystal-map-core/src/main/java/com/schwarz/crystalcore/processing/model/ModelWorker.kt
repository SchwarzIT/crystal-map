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
import com.schwarz.crystalcore.model.typeconverter.TypeConverterHolderForEntityGeneration
import com.schwarz.crystalcore.processing.Worker
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeName

class ModelWorker<T>(
    override val logger: ILogger<T>,
    override val codeGenerator: ICodeGenerator,
    override val settings: ISettings,
    override val workSet: ModelWorkSet<T>
) :
    Worker<ModelWorkSet<T>, T> {

    private var documentationGenerator: DocumentationGenerator? = null
    private var entityRelationshipGenerator: EntityRelationshipGenerator? = null
    private var schemaGenerator: SchemaGenerator? = null

    override fun init() {
        settings.documentationPath?.let {
            documentationGenerator = DocumentationGenerator(it, settings.documentationFilename ?: "default.html")
        }
        settings.schemaPath?.let {
            schemaGenerator = SchemaGenerator(
                it,
                settings.schemaFilename ?: "schema.json"
            )
        }

        settings.entityRelationshipPath?.let {
            entityRelationshipGenerator = EntityRelationshipGenerator(it, settings.entityRelationshipFilename ?: "default.gv")
        }
    }

    override fun doWork(workSet: ModelWorkSet<T>, useSuspend: Boolean) {
        val typeConvertersByConvertedClass: Map<TypeName, TypeConverterHolderForEntityGeneration> = (workSet.typeConverters + workSet.importedTypeConverters).associateBy { it.domainClassTypeName }

        workSet.typeConverters.forEach {
            codeGenerator.generate(TypeConverterObjectGeneration.generateTypeConverterObject(it), settings)
        }

        workSet.typeConverterExporters.forEach {
            codeGenerator.generate(TypeConverterExporterObjectGeneration.generateTypeConverterExporterObject(it, workSet.typeConverters), settings)
        }

        val generatedInterfaces = mutableSetOf<String>()

        for (baseModelHolder in workSet.bases) {
            generateInterface(generatedInterfaces, baseModelHolder, useSuspend, typeConvertersByConvertedClass)
        }

        processAndFixAccessors(workSet.entities, generatedInterfaces, useSuspend, typeConvertersByConvertedClass) {
            EntityGeneration().generateModel(it, useSuspend, typeConvertersByConvertedClass)
        }

        processAndFixAccessors(workSet.wrappers, generatedInterfaces, useSuspend, typeConvertersByConvertedClass) {
            WrapperGeneration().generateModel(it, useSuspend, typeConvertersByConvertedClass)
        }

        process(workSet.schemas) {
            SchemaGeneration().generateModel(it, workSet.schemaClassPaths, typeConvertersByConvertedClass)
        }

        documentationGenerator?.generate()
        entityRelationshipGenerator?.generate()
        schemaGenerator?.generate()
    }

    private fun process(schemaModels: List<SchemaClassHolder<T>>, generate: (SchemaClassHolder<T>) -> FileSpec) {
        for (model in schemaModels) {
            documentationGenerator?.addEntitySegments(model)
            schemaGenerator?.addEntity(model)
            entityRelationshipGenerator?.addEntityNodes(model)
            generate(model).apply {
                codeGenerator.generate(this, settings)
            }
        }
    }

    private fun <E : BaseEntityHolder<T>> processAndFixAccessors(
        models: List<E>,
        generatedInterfaces: MutableSet<String>,
        useSuspend: Boolean,
        typeConvertersByConvertedClass: Map<TypeName, TypeConverterHolderForEntityGeneration>,
        generate: (E) -> FileSpec
    ) {
        for (model in models) {
            generateInterface(generatedInterfaces, model, useSuspend, typeConvertersByConvertedClass)
            documentationGenerator?.addEntitySegments(model)
            schemaGenerator?.addEntity(model)
            entityRelationshipGenerator?.addEntityNodes(model)
            generate(model).apply {
                if (model.generateAccessors.isNotEmpty()) {
                    codeGenerator.generateAndFixAccessors(
                        this,
                        model.generateAccessors,
                        settings
                    )
                } else {
                    codeGenerator.generate(
                        this,
                        settings
                    )
                }
            }
        }
    }

    private fun generateInterface(
        generatedInterfaces: MutableSet<String>,
        holder: BaseEntityHolder<T>,
        useSuspend: Boolean,
        typeConvertersByConvertedClass: Map<TypeName, TypeConverterHolderForEntityGeneration>
    ) {
        if (generatedInterfaces.contains(holder.sourceClazzSimpleName).not()) {
            codeGenerator.generate(
                CommonInterfaceGeneration().generateModel(
                    holder,
                    useSuspend,
                    typeConvertersByConvertedClass
                ),
                settings
            )
            generatedInterfaces.add(holder.sourceClazzSimpleName)
        }
    }
}
