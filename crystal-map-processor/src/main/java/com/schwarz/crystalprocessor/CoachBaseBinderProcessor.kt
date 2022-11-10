package com.schwarz.crystalprocessor

import com.google.auto.service.AutoService
import com.schwarz.crystalprocessor.CoachBaseBinderProcessor.Companion.FRAMEWORK_DOCUMENTATION_FILENAME_OPTION_NAME
import com.schwarz.crystalprocessor.CoachBaseBinderProcessor.Companion.FRAMEWORK_DOCUMENTATION_PATH_OPTION_NAME
import com.schwarz.crystalprocessor.CoachBaseBinderProcessor.Companion.FRAMEWORK_SCHEMA_FILENAME_OPTION_NAME
import com.schwarz.crystalprocessor.CoachBaseBinderProcessor.Companion.FRAMEWORK_SCHEMA_PATH_OPTION_NAME
import com.schwarz.crystalprocessor.CoachBaseBinderProcessor.Companion.FRAMEWORK_USE_SUSPEND_OPTION_NAME
import com.schwarz.crystalprocessor.CoachBaseBinderProcessor.Companion.KAPT_KOTLIN_GENERATED_OPTION_NAME
import com.schwarz.crystalprocessor.generation.CodeGenerator
import com.schwarz.crystalprocessor.processing.Worker
import com.schwarz.crystalprocessor.processing.mapper.MapperWorker
import com.schwarz.crystalprocessor.processing.model.ModelWorker
import com.schwarz.crystalapi.*
import com.schwarz.crystalapi.mapify.Mapper
import com.schwarz.crystalapi.query.Queries
import com.schwarz.crystalapi.query.Query
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor::class)
@SupportedOptions(KAPT_KOTLIN_GENERATED_OPTION_NAME, FRAMEWORK_USE_SUSPEND_OPTION_NAME, FRAMEWORK_DOCUMENTATION_PATH_OPTION_NAME, FRAMEWORK_DOCUMENTATION_FILENAME_OPTION_NAME, FRAMEWORK_SCHEMA_PATH_OPTION_NAME, FRAMEWORK_SCHEMA_FILENAME_OPTION_NAME)
class CoachBaseBinderProcessor : AbstractProcessor() {

    private lateinit var mLogger: Logger

    private lateinit var mCodeGenerator: CodeGenerator

    private var useSuspend: Boolean = false

    private lateinit var workers: Set<Worker<*>>

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
        const val FRAMEWORK_USE_SUSPEND_OPTION_NAME = "useSuspend"
        const val FRAMEWORK_DOCUMENTATION_PATH_OPTION_NAME = "entityframework.documentation.generated"
        const val FRAMEWORK_DOCUMENTATION_FILENAME_OPTION_NAME = "entityframework.documentation.fileName"
        const val FRAMEWORK_ENTITY_RELATIONSHIP_PATH_OPTION_NAME = "entityframework.documentation.entityrelationship.generated"
        const val FRAMEWORK_ENTITY_RELATIONSHIP_FILENAME_OPTION_NAME = "entityframework.documentation.entityrelationship.fileName"
        const val FRAMEWORK_SCHEMA_PATH_OPTION_NAME = "entityframework.schema.generated"
        const val FRAMEWORK_SCHEMA_FILENAME_OPTION_NAME = "entityframework.schema.fileName"
    }

    @Synchronized
    override fun init(processingEnvironment: ProcessingEnvironment) {
        useSuspend = processingEnvironment.options?.getOrDefault(FRAMEWORK_USE_SUSPEND_OPTION_NAME, "false")?.toBoolean()
            ?: false
        mLogger = Logger(processingEnvironment)
        mCodeGenerator = CodeGenerator(processingEnvironment.filer)

        ProcessingContext.env = processingEnvironment

        workers = setOf(
            ModelWorker(mLogger, mCodeGenerator, processingEnvironment),
            MapperWorker(mLogger, mCodeGenerator, processingEnvironment)
        )

        workers.forEach { it.init() }

        super.init(processingEnvironment)
    }

    override fun process(set: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        ProcessingContext.roundEnv = roundEnv

        for (worker in workers) {
            try {
                if (!worker.invoke(roundEnv, useSuspend)) {
                    // error in worker no further processing
                    return true
                }
            } catch (e: PostValidationException) {
                mLogger.abortWithError(e)
                return true
            }
        }

        return true // no further processing of this annotation type
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return setOf(Field::class.java.canonicalName, Entity::class.java.canonicalName, MapWrapper::class.java.canonicalName, Queries::class.java.canonicalName, Query::class.java.canonicalName, GenerateAccessor::class.java.canonicalName, Mapper::class.java.canonicalName, Reduces::class.java.canonicalName, Reduce::class.java.canonicalName).toMutableSet()
    }
}
