package com.kaufland.processing.model

import com.kaufland.CoachBaseBinderProcessor
import com.kaufland.Logger
import com.kaufland.documentation.DocumentationGenerator
import com.kaufland.documentation.EntityRelationshipGenerator
import com.kaufland.generation.CodeGenerator
import com.kaufland.generation.model.CommonInterfaceGeneration
import com.kaufland.generation.model.EntityGeneration
import com.kaufland.generation.model.WrapperGeneration
import com.kaufland.meta.SchemaGenerator
import com.kaufland.model.entity.BaseEntityHolder
import com.kaufland.processing.Worker
import com.squareup.kotlinpoet.FileSpec
import kaufland.com.coachbasebinderapi.BaseModel
import kaufland.com.coachbasebinderapi.Entity
import kaufland.com.coachbasebinderapi.MapWrapper
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment

class ModelWorker(override val logger: Logger, override val codeGenerator: CodeGenerator, override val processingEnv: ProcessingEnvironment) : Worker<ModelWorkSet> {

    private var documentationGenerator: DocumentationGenerator? = null
    private var entityRelationshipGenerator: EntityRelationshipGenerator? = null
    private var schemaGenerator: SchemaGenerator? = null

    override fun init() {
        processingEnv.options[CoachBaseBinderProcessor.FRAMEWORK_DOCUMENTATION_PATH_OPTION_NAME]?.let {
            documentationGenerator = DocumentationGenerator(it, processingEnv.options.getOrDefault(CoachBaseBinderProcessor.FRAMEWORK_DOCUMENTATION_FILENAME_OPTION_NAME, "default.html"))

        }
        processingEnv.options[CoachBaseBinderProcessor.FRAMEWORK_SCHEMA_PATH_OPTION_NAME]?.let {
            schemaGenerator = SchemaGenerator(it, processingEnv.options.getOrDefault(CoachBaseBinderProcessor.FRAMEWORK_SCHEMA_FILENAME_OPTION_NAME, "schema.json"))
        }

        processingEnv.options[CoachBaseBinderProcessor.FRAMEWORK_ENTITY_RELATIONSHIP_PATH_OPTION_NAME]?.let {
            entityRelationshipGenerator = EntityRelationshipGenerator(it, processingEnv.options.getOrDefault(CoachBaseBinderProcessor.FRAMEWORK_ENTITY_RELATIONSHIP_FILENAME_OPTION_NAME, "default.gv"))
        }
    }

    override fun doWork(workSet: ModelWorkSet, useSuspend: Boolean) {
        var generatedInterfaces = mutableSetOf<String>()

        for (baseModelHolder in workSet.bases) {
            generateInterface(generatedInterfaces, baseModelHolder)
        }

        process(workSet.entities, generatedInterfaces) {
            EntityGeneration().generateModel(it, useSuspend)
        }

        process(workSet.wrappers, generatedInterfaces) {
            WrapperGeneration().generateModel(it, useSuspend)
        }

        documentationGenerator?.generate()
        entityRelationshipGenerator?.generate()
        schemaGenerator?.generate()
    }

    private fun <T : BaseEntityHolder> process(models: List<T>, generatedInterfaces: MutableSet<String>, generate: (T) -> FileSpec) {

        for (model in models) {
            generateInterface(generatedInterfaces, model)
            documentationGenerator?.addEntitySegments(model)
            schemaGenerator?.addEntity(model)
            entityRelationshipGenerator?.addEntityNodes(model)
            generate(model)?.apply {
                codeGenerator.generate(this, processingEnv)
            }
        }
    }


    private fun generateInterface(generatedInterfaces: MutableSet<String>, holder: BaseEntityHolder) {
        if (generatedInterfaces.contains(holder.sourceClazzSimpleName).not()) {
            codeGenerator.generate(CommonInterfaceGeneration().generateModel(holder), processingEnv)
            generatedInterfaces.add(holder.sourceClazzSimpleName)
        }
    }

    override fun evaluateWorkSet(roundEnv: RoundEnvironment): ModelWorkSet = ModelWorkSet(
            allEntityElements = roundEnv.getElementsAnnotatedWith(Entity::class.java),
            allWrapperElements = roundEnv.getElementsAnnotatedWith(MapWrapper::class.java),
            allBaseModelElements = roundEnv.getElementsAnnotatedWith(BaseModel::class.java)
    )
}