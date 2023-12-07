package com.schwarz.crystalprocessor.processing.model

import com.schwarz.crystalprocessor.CoachBaseBinderProcessor
import com.schwarz.crystalprocessor.Logger
import com.schwarz.crystalprocessor.documentation.DocumentationGenerator
import com.schwarz.crystalprocessor.documentation.EntityRelationshipGenerator
import com.schwarz.crystalprocessor.generation.CodeGenerator
import com.schwarz.crystalprocessor.generation.model.CommonInterfaceGeneration
import com.schwarz.crystalprocessor.generation.model.EntityGeneration
import com.schwarz.crystalprocessor.generation.model.WrapperGeneration
import com.schwarz.crystalprocessor.meta.SchemaGenerator
import com.schwarz.crystalprocessor.model.entity.BaseEntityHolder
import com.schwarz.crystalprocessor.processing.Worker
import com.squareup.kotlinpoet.FileSpec
import com.schwarz.crystalapi.BaseModel
import com.schwarz.crystalapi.Entity
import com.schwarz.crystalapi.MapWrapper
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
        var generatedInterfaces = mutableSetOf<String>()

        for (baseModelHolder in workSet.bases) {
            generateInterface(generatedInterfaces, baseModelHolder, useSuspend)
        }

        process(workSet.entities, generatedInterfaces, useSuspend) {
            EntityGeneration().generateModel(it, useSuspend)
        }

        process(workSet.wrappers, generatedInterfaces, useSuspend) {
            WrapperGeneration().generateModel(it, useSuspend)
        }

        documentationGenerator?.generate()
        entityRelationshipGenerator?.generate()
        schemaGenerator?.generate()
    }

    private fun <T : BaseEntityHolder> process(models: List<T>, generatedInterfaces: MutableSet<String>, useSuspend: Boolean, generate: (T) -> FileSpec) {
        for (model in models) {
            generateInterface(generatedInterfaces, model, useSuspend)
            documentationGenerator?.addEntitySegments(model)
            schemaGenerator?.addEntity(model)
            entityRelationshipGenerator?.addEntityNodes(model)
            generate(model)?.apply {
                codeGenerator.generate(this, processingEnv)
            }
        }
    }

    private fun generateInterface(
        generatedInterfaces: MutableSet<String>,
        holder: BaseEntityHolder,
        useSuspend: Boolean
    ) {
        if (generatedInterfaces.contains(holder.sourceClazzSimpleName).not()) {
            codeGenerator.generate(CommonInterfaceGeneration().generateModel(holder, useSuspend), processingEnv)
            generatedInterfaces.add(holder.sourceClazzSimpleName)
        }
    }

    override fun evaluateWorkSet(roundEnv: RoundEnvironment): ModelWorkSet = ModelWorkSet(
        allEntityElements = roundEnv.getElementsAnnotatedWith(Entity::class.java),
        allWrapperElements = roundEnv.getElementsAnnotatedWith(MapWrapper::class.java),
        allBaseModelElements = roundEnv.getElementsAnnotatedWith(BaseModel::class.java)
    )
}
