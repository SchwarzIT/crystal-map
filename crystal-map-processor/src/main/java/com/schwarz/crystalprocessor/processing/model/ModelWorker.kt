package com.schwarz.crystalprocessor.processing.model

import com.schwarz.crystalapi.BaseModel
import com.schwarz.crystalapi.Entity
import com.schwarz.crystalapi.MapWrapper
import com.schwarz.crystalapi.SchemaClass
import com.schwarz.crystalapi.TypeConverter
import com.schwarz.crystalapi.TypeConverterExporter
import com.schwarz.crystalapi.TypeConverterImporter
import com.schwarz.crystalprocessor.CoachBaseBinderProcessor
import com.schwarz.crystalprocessor.Logger
import com.schwarz.crystalprocessor.documentation.DocumentationGenerator
import com.schwarz.crystalprocessor.documentation.EntityRelationshipGenerator
import com.schwarz.crystalprocessor.generation.CodeGenerator
import com.schwarz.crystalprocessor.generation.model.CommonInterfaceGeneration
import com.schwarz.crystalprocessor.generation.model.EntityGeneration
import com.schwarz.crystalprocessor.generation.model.SchemaGeneration
import com.schwarz.crystalprocessor.generation.model.TypeConverterExporterObjectGeneration
import com.schwarz.crystalprocessor.generation.model.TypeConverterObjectGeneration
import com.schwarz.crystalprocessor.generation.model.WrapperGeneration
import com.schwarz.crystalprocessor.meta.SchemaGenerator
import com.schwarz.crystalprocessor.model.entity.BaseEntityHolder
import com.schwarz.crystalprocessor.model.typeconverter.TypeConverterHolderForEntityGeneration
import com.schwarz.crystalprocessor.processing.Worker
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeName
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment

class ModelWorker(override val logger: Logger, override val codeGenerator: CodeGenerator, override val processingEnv: ProcessingEnvironment) :
    Worker<ModelWorkSet> {

    private var documentationGenerator: DocumentationGenerator? = null
    private var entityRelationshipGenerator: EntityRelationshipGenerator? = null
    private var schemaGenerator: SchemaGenerator? = null

    override fun init() {
        processingEnv.options[CoachBaseBinderProcessor.FRAMEWORK_DOCUMENTATION_PATH_OPTION_NAME]?.let {
            documentationGenerator = DocumentationGenerator(it, processingEnv.options.getOrDefault(CoachBaseBinderProcessor.FRAMEWORK_DOCUMENTATION_FILENAME_OPTION_NAME, "default.html"))
        }
        processingEnv.options[CoachBaseBinderProcessor.FRAMEWORK_SCHEMA_PATH_OPTION_NAME]?.let {
            schemaGenerator = SchemaGenerator(
                it,
                processingEnv.options.getOrDefault(
                    CoachBaseBinderProcessor.FRAMEWORK_SCHEMA_FILENAME_OPTION_NAME,
                    "schema.json"
                )
            )
        }

        processingEnv.options[CoachBaseBinderProcessor.FRAMEWORK_ENTITY_RELATIONSHIP_PATH_OPTION_NAME]?.let {
            entityRelationshipGenerator = EntityRelationshipGenerator(it, processingEnv.options.getOrDefault(CoachBaseBinderProcessor.FRAMEWORK_ENTITY_RELATIONSHIP_FILENAME_OPTION_NAME, "default.gv"))
        }
    }

    override fun doWork(workSet: ModelWorkSet, useSuspend: Boolean) {
        val typeConvertersByConvertedClass: Map<TypeName, TypeConverterHolderForEntityGeneration> = (workSet.typeConverters + workSet.importedTypeConverters).associateBy { it.domainClassTypeName }

        workSet.typeConverters.forEach {
            codeGenerator.generate(TypeConverterObjectGeneration.generateTypeConverterObject(it), processingEnv)
        }

        workSet.typeConverterExporters.forEach {
            codeGenerator.generate(TypeConverterExporterObjectGeneration.generateTypeConverterExporterObject(it, workSet.typeConverters), processingEnv)
        }

        val generatedInterfaces = mutableSetOf<String>()

        for (baseModelHolder in workSet.bases) {
            generateInterface(generatedInterfaces, baseModelHolder, useSuspend, typeConvertersByConvertedClass)
        }

        process(workSet.entities, generatedInterfaces, useSuspend, typeConvertersByConvertedClass) {
            EntityGeneration().generateModel(it, useSuspend, typeConvertersByConvertedClass)
        }

        process(workSet.wrappers, generatedInterfaces, useSuspend, typeConvertersByConvertedClass) {
            WrapperGeneration().generateModel(it, useSuspend, typeConvertersByConvertedClass)
        }

        process(workSet.schemas, generatedInterfaces, useSuspend, typeConvertersByConvertedClass) {
            SchemaGeneration().generateModel(it, workSet.schemaClassPaths)
        }

        documentationGenerator?.generate()
        entityRelationshipGenerator?.generate()
        schemaGenerator?.generate()
    }

    private fun <T : BaseEntityHolder> process(
        models: List<T>,
        generatedInterfaces: MutableSet<String>,
        useSuspend: Boolean,
        typeConvertersByConvertedClass: Map<TypeName, TypeConverterHolderForEntityGeneration>,
        generate: (T) -> FileSpec
    ) {
        for (model in models) {
            generateInterface(generatedInterfaces, model, useSuspend, typeConvertersByConvertedClass)
            documentationGenerator?.addEntitySegments(model)
            schemaGenerator?.addEntity(model)
            entityRelationshipGenerator?.addEntityNodes(model)
            generate(model).apply {
                codeGenerator.generate(this, processingEnv)
            }
        }
    }

    private fun generateInterface(
        generatedInterfaces: MutableSet<String>,
        holder: BaseEntityHolder,
        useSuspend: Boolean,
        typeConvertersByConvertedClass: Map<TypeName, TypeConverterHolderForEntityGeneration>
    ) {
        if (generatedInterfaces.contains(holder.sourceClazzSimpleName).not()) {
            codeGenerator.generate(CommonInterfaceGeneration().generateModel(holder, useSuspend, typeConvertersByConvertedClass), processingEnv)
            generatedInterfaces.add(holder.sourceClazzSimpleName)
        }
    }

    override fun evaluateWorkSet(roundEnv: RoundEnvironment): ModelWorkSet = ModelWorkSet(
        allEntityElements = roundEnv.getElementsAnnotatedWith(Entity::class.java),
        allWrapperElements = roundEnv.getElementsAnnotatedWith(MapWrapper::class.java),
        allSchemaClassElements = roundEnv.getElementsAnnotatedWith(SchemaClass::class.java),
        allBaseModelElements = roundEnv.getElementsAnnotatedWith(BaseModel::class.java),
        allTypeConverterElements = roundEnv.getElementsAnnotatedWith(TypeConverter::class.java),
        allTypeConverterExporterElements = roundEnv.getElementsAnnotatedWith(TypeConverterExporter::class.java),
        allTypeConverterImporterElements = roundEnv.getElementsAnnotatedWith(TypeConverterImporter::class.java)
    )
}
