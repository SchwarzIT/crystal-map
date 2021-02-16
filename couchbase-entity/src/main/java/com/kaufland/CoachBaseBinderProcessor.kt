package com.kaufland

import com.google.auto.service.AutoService
import com.kaufland.CoachBaseBinderProcessor.Companion.FRAMEWORK_DOCUMENTATION_FILENAME_OPTION_NAME
import com.kaufland.CoachBaseBinderProcessor.Companion.FRAMEWORK_DOCUMENTATION_PATH_OPTION_NAME
import com.kaufland.CoachBaseBinderProcessor.Companion.FRAMEWORK_SCHEME_FILENAME_OPTION_NAME
import com.kaufland.CoachBaseBinderProcessor.Companion.FRAMEWORK_SCHEME_PATH_OPTION_NAME
import com.kaufland.CoachBaseBinderProcessor.Companion.FRAMEWORK_USE_SUSPEND_OPTION_NAME
import com.kaufland.CoachBaseBinderProcessor.Companion.KAPT_KOTLIN_GENERATED_OPTION_NAME
import com.kaufland.documentation.DocumentationGenerator
import com.kaufland.documentation.EntityRelationshipGenerator
import com.kaufland.generation.CodeGenerator
import com.kaufland.generation.CommonInterfaceGeneration
import com.kaufland.generation.EntityGeneration
import com.kaufland.generation.WrapperGeneration
import com.kaufland.meta.SchemeGenerator
import com.kaufland.model.entity.BaseEntityHolder
import com.kaufland.processing.ModelWorkSet
import com.squareup.kotlinpoet.FileSpec
import kaufland.com.coachbasebinderapi.*
import kaufland.com.coachbasebinderapi.query.Queries
import kaufland.com.coachbasebinderapi.query.Query
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement


@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor::class)
@SupportedOptions(KAPT_KOTLIN_GENERATED_OPTION_NAME, FRAMEWORK_USE_SUSPEND_OPTION_NAME, FRAMEWORK_DOCUMENTATION_PATH_OPTION_NAME, FRAMEWORK_DOCUMENTATION_FILENAME_OPTION_NAME, FRAMEWORK_SCHEME_PATH_OPTION_NAME, FRAMEWORK_SCHEME_FILENAME_OPTION_NAME)
class CoachBaseBinderProcessor : AbstractProcessor() {

    private lateinit var mLogger: Logger

    private lateinit var mCodeGenerator: CodeGenerator

    private var useSuspend: Boolean = false

    private var documentationGenerator: DocumentationGenerator? = null
    private var entityRelationshipGenerator: EntityRelationshipGenerator? = null
    private var metaDescriptionGenerator: SchemeGenerator? = null

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
        const val FRAMEWORK_USE_SUSPEND_OPTION_NAME = "useSuspend"
        const val FRAMEWORK_DOCUMENTATION_PATH_OPTION_NAME = "entityframework.documentation.generated"
        const val FRAMEWORK_DOCUMENTATION_FILENAME_OPTION_NAME = "entityframework.documentation.fileName"
        const val FRAMEWORK_ENTITY_RELATIONSHIP_PATH_OPTION_NAME = "entityframework.documentation.entityrelationship.generated"
        const val FRAMEWORK_ENTITY_RELATIONSHIP_FILENAME_OPTION_NAME = "entityframework.documentation.entityrelationship.fileName"
        const val FRAMEWORK_SCHEME_PATH_OPTION_NAME = "entityframework.scheme.generated"
        const val FRAMEWORK_SCHEME_FILENAME_OPTION_NAME = "entityframework.scheme.fileName"


    }

    @Synchronized
    override fun init(processingEnvironment: ProcessingEnvironment) {
        useSuspend = processingEnvironment.options?.getOrDefault(FRAMEWORK_USE_SUSPEND_OPTION_NAME, "false")?.toBoolean()
                ?: false
        mLogger = Logger(processingEnvironment)
        mCodeGenerator = CodeGenerator(processingEnvironment.filer)

        processingEnvironment.options[FRAMEWORK_DOCUMENTATION_PATH_OPTION_NAME]?.let {
            documentationGenerator = DocumentationGenerator(it, processingEnvironment.options.getOrDefault(FRAMEWORK_DOCUMENTATION_FILENAME_OPTION_NAME, "default.html"))

        }
        processingEnvironment.options[FRAMEWORK_SCHEME_PATH_OPTION_NAME]?.let {
            metaDescriptionGenerator = SchemeGenerator(it, processingEnvironment.options.getOrDefault(FRAMEWORK_SCHEME_FILENAME_OPTION_NAME, "scheme.json"))
        }

        processingEnvironment.options[FRAMEWORK_ENTITY_RELATIONSHIP_PATH_OPTION_NAME]?.let {
            entityRelationshipGenerator = EntityRelationshipGenerator(it, processingEnvironment.options.getOrDefault(FRAMEWORK_ENTITY_RELATIONSHIP_FILENAME_OPTION_NAME, "default.gv"))
        }
        super.init(processingEnvironment)
    }

    override fun process(set: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {

        var generatedInterfaces = mutableSetOf<String>()

        val workSet = ModelWorkSet(
                mLogger,
                allEntityElements = roundEnv.getElementsAnnotatedWith(Entity::class.java),
                allWrapperElements = roundEnv.getElementsAnnotatedWith(MapWrapper::class.java),
                allBaseModelElements = roundEnv.getElementsAnnotatedWith(BaseModel::class.java))

        if (mLogger.hasErrors()) {
            return true
        }

        workSet.loadModels()

        if (mLogger.hasErrors()) {
            return true
        }

        for (baseModelHolder in workSet.bases) {
            generateInterface(generatedInterfaces, baseModelHolder)
        }

        process(workSet.entities, generatedInterfaces){
            EntityGeneration().generateModel(it, useSuspend)
        }

        process(workSet.wrappers, generatedInterfaces){
            WrapperGeneration().generateModel(it, useSuspend)
        }

        documentationGenerator?.generate()
        entityRelationshipGenerator?.generate()
        metaDescriptionGenerator?.generate()

        return true // no further processing of this annotation type
    }

    private fun <T : BaseEntityHolder> process(models: List<T>, generatedInterfaces: MutableSet<String>, generate : (T) -> FileSpec){

        for (model in models) {
            generateInterface(generatedInterfaces, model)
            documentationGenerator?.addEntitySegments(model)
            metaDescriptionGenerator?.addEntity(model)
            entityRelationshipGenerator?.addEntityNodes(model)
            generate(model)?.apply {
                mCodeGenerator!!.generate(this, processingEnv)
            }
        }
    }



    private fun generateInterface(generatedInterfaces: MutableSet<String>, holder: BaseEntityHolder) {
        if (generatedInterfaces.contains(holder.sourceClazzSimpleName).not()) {
            mCodeGenerator!!.generate(CommonInterfaceGeneration().generateModel(holder), processingEnv)
            generatedInterfaces.add(holder.sourceClazzSimpleName)
        }
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return setOf(Field::class.java.canonicalName, Entity::class.java.canonicalName, MapWrapper::class.java.canonicalName, Queries::class.java.canonicalName, Query::class.java.canonicalName, GenerateAccessor::class.java.canonicalName).toMutableSet()
    }


}
