package com.schwarz.crystalprocessor

import com.google.auto.service.AutoService
import com.schwarz.crystalapi.BaseModel
import com.schwarz.crystalapi.Entity
import com.schwarz.crystalapi.Field
import com.schwarz.crystalapi.GenerateAccessor
import com.schwarz.crystalapi.MapWrapper
import com.schwarz.crystalapi.Reduce
import com.schwarz.crystalapi.Reduces
import com.schwarz.crystalapi.SchemaClass
import com.schwarz.crystalapi.TypeConverter
import com.schwarz.crystalapi.TypeConverterExporter
import com.schwarz.crystalapi.TypeConverterImporter
import com.schwarz.crystalapi.mapify.Mapper
import com.schwarz.crystalapi.query.Queries
import com.schwarz.crystalapi.query.Query
import com.schwarz.crystalcore.PostValidationException
import com.schwarz.crystalcore.model.source.ISourceMapperModel
import com.schwarz.crystalcore.model.source.ISourceModel
import com.schwarz.crystalcore.processing.Worker
import com.schwarz.crystalcore.processing.mapper.MapperWorkSet
import com.schwarz.crystalcore.processing.mapper.MapperWorker
import com.schwarz.crystalcore.processing.model.ModelWorkSet
import com.schwarz.crystalcore.processing.model.ModelWorker
import com.schwarz.crystalprocessor.CoachBaseBinderProcessor.Companion.FRAMEWORK_DOCUMENTATION_FILENAME_OPTION_NAME
import com.schwarz.crystalprocessor.CoachBaseBinderProcessor.Companion.FRAMEWORK_DOCUMENTATION_PATH_OPTION_NAME
import com.schwarz.crystalprocessor.CoachBaseBinderProcessor.Companion.FRAMEWORK_SCHEMA_FILENAME_OPTION_NAME
import com.schwarz.crystalprocessor.CoachBaseBinderProcessor.Companion.FRAMEWORK_SCHEMA_PATH_OPTION_NAME
import com.schwarz.crystalprocessor.CoachBaseBinderProcessor.Companion.FRAMEWORK_USE_SUSPEND_OPTION_NAME
import com.schwarz.crystalprocessor.CoachBaseBinderProcessor.Companion.KAPT_KOTLIN_GENERATED_OPTION_NAME
import com.schwarz.crystalprocessor.generation.CodeGenerator
import com.schwarz.crystalprocessor.model.source.SourceMapperModel
import com.schwarz.crystalprocessor.model.source.SourceModel
import com.schwarz.crystalprocessor.validation.mapper.PreMapperValidation
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedOptions
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor::class)
@SupportedOptions(
    KAPT_KOTLIN_GENERATED_OPTION_NAME,
    FRAMEWORK_USE_SUSPEND_OPTION_NAME,
    FRAMEWORK_DOCUMENTATION_PATH_OPTION_NAME,
    FRAMEWORK_DOCUMENTATION_FILENAME_OPTION_NAME,
    FRAMEWORK_SCHEMA_PATH_OPTION_NAME,
    FRAMEWORK_SCHEMA_FILENAME_OPTION_NAME,
)
class CoachBaseBinderProcessor : AbstractProcessor() {
    private lateinit var mLogger: Logger

    private lateinit var mCodeGenerator: CodeGenerator

    private var useSuspend: Boolean = false

    private lateinit var mSettings: ProcessingEnvironmentWrapper

    private lateinit var workers: Set<Worker<*, Element>>

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
        const val FRAMEWORK_USE_SUSPEND_OPTION_NAME = "crystal.entityframework.useSuspend"
        const val FRAMEWORK_DOCUMENTATION_PATH_OPTION_NAME =
            "crystal.entityframework.documentation.generated"
        const val FRAMEWORK_DOCUMENTATION_FILENAME_OPTION_NAME =
            "crystal.entityframework.documentation.fileName"
        const val FRAMEWORK_ENTITY_RELATIONSHIP_PATH_OPTION_NAME =
            "crystal.entityframework.documentation.entityrelationship.generated"
        const val FRAMEWORK_ENTITY_RELATIONSHIP_FILENAME_OPTION_NAME =
            "crystal.entityframework.documentation.entityrelationship.fileName"
        const val FRAMEWORK_SCHEMA_PATH_OPTION_NAME = "crystal.entityframework.schema.generated"
        const val FRAMEWORK_SCHEMA_FILENAME_OPTION_NAME = "crystal.entityframework.schema.fileName"
    }

    @Synchronized
    override fun init(processingEnvironment: ProcessingEnvironment) {
        useSuspend =
            processingEnvironment.options
                ?.getOrDefault(FRAMEWORK_USE_SUSPEND_OPTION_NAME, "false")
                ?.toBoolean()
                ?: false
        mLogger = Logger(processingEnvironment)
        mCodeGenerator = CodeGenerator(processingEnvironment.filer)
        mSettings = ProcessingEnvironmentWrapper(processingEnvironment)

        ProcessingContext.env = processingEnvironment

        super.init(processingEnvironment)
    }

    override fun process(
        set: Set<TypeElement>,
        roundEnv: RoundEnvironment,
    ): Boolean {
        ProcessingContext.roundEnv = roundEnv
        workers =
            setOf(
                ModelWorker<Element>(
                    mLogger,
                    mCodeGenerator,
                    mSettings,
                    ModelWorkSet(
                        allEntityElements =
                            roundEnv
                                .getElementsAnnotatedWith(Entity::class.java)
                                .toSourceModel(),
                        allWrapperElements =
                            roundEnv
                                .getElementsAnnotatedWith(MapWrapper::class.java)
                                .toSourceModel(),
                        allSchemaClassElements =
                            roundEnv
                                .getElementsAnnotatedWith(SchemaClass::class.java)
                                .toSourceModel(),
                        allBaseModelElements =
                            roundEnv
                                .getElementsAnnotatedWith(BaseModel::class.java)
                                .toSourceModel(),
                        allTypeConverterElements =
                            roundEnv
                                .getElementsAnnotatedWith(TypeConverter::class.java)
                                .toSourceModel(),
                        allTypeConverterExporterElements =
                            roundEnv
                                .getElementsAnnotatedWith(
                                    TypeConverterExporter::class.java,
                                ).toSourceModel(),
                        allTypeConverterImporterElements =
                            roundEnv
                                .getElementsAnnotatedWith(
                                    TypeConverterImporter::class.java,
                                ).toSourceModel(),
                    ),
                ),
                MapperWorker(
                    mLogger,
                    mCodeGenerator,
                    mSettings,
                    MapperWorkSet(
                        allMapperElements =
                            roundEnv
                                .getElementsAnnotatedWith(
                                    Mapper::class.java,
                                ).toMapperSourceModel(),
                        PreMapperValidation::validate,
                    ),
                ),
            )

        workers.forEach { it.init() }

        for (worker in workers) {
            try {
                if (!worker.invoke(useSuspend)) {
                    // error in worker no further processing
                    return true
                }
            } catch (e: PostValidationException) {
                mLogger.abortWithError(e.message ?: "", unboxError(e.causingElements), e)
                return true
            }
        }

        return true // no further processing of this annotation type
    }

    private fun unboxError(value: Any?): List<Element> =
        when (value) {
            is List<*> -> value.map { it as Element }
            is Element -> listOf(value as Element)
            else -> emptyList()
        }

    private fun Set<Element>.toSourceModel(): Set<ISourceModel<Element>> =
        map {
            SourceModel(it)
        }.toSet()

    private fun Set<Element>.toMapperSourceModel(): Set<ISourceMapperModel<Element>> =
        map {
            SourceMapperModel(it)
        }.toSet()

    override fun getSupportedAnnotationTypes(): MutableSet<String> =
        setOf(
            Field::class.java.canonicalName,
            Entity::class.java.canonicalName,
            MapWrapper::class.java.canonicalName,
            SchemaClass::class.java.canonicalName,
            Queries::class.java.canonicalName,
            Query::class.java.canonicalName,
            GenerateAccessor::class.java.canonicalName,
            Mapper::class.java.canonicalName,
            Reduces::class.java.canonicalName,
            Reduce::class.java.canonicalName,
            TypeConverter::class.java.canonicalName,
            TypeConverterExporter::class.java.canonicalName,
            TypeConverterImporter::class.java.canonicalName,
        ).toMutableSet()
}
