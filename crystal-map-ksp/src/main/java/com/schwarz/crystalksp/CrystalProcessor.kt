package com.schwarz.crystalksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.schwarz.crystalapi.BaseModel
import com.schwarz.crystalapi.Entity
import com.schwarz.crystalapi.MapWrapper
import com.schwarz.crystalapi.SchemaClass
import com.schwarz.crystalapi.TypeConverter
import com.schwarz.crystalapi.TypeConverterExporter
import com.schwarz.crystalapi.TypeConverterImporter
import com.schwarz.crystalapi.mapify.Mapper
import com.schwarz.crystalcore.PostValidationException
import com.schwarz.crystalcore.model.source.ISourceMapperModel
import com.schwarz.crystalcore.model.source.ISourceModel
import com.schwarz.crystalcore.processing.Worker
import com.schwarz.crystalcore.processing.mapper.MapperWorkSet
import com.schwarz.crystalcore.processing.mapper.MapperWorker
import com.schwarz.crystalcore.processing.model.ModelWorkSet
import com.schwarz.crystalcore.processing.model.ModelWorker
import com.schwarz.crystalksp.generation.KSPCodeGenerator
import com.schwarz.crystalksp.model.source.SourceMapperModel
import com.schwarz.crystalksp.model.source.SourceModel
import com.schwarz.crystalksp.validation.mapper.PreMapperValidation
import kotlin.metadata.ClassName

class CrystalProcessor(
    codeGenerator: CodeGenerator,
    val logger: KSPLogger,
    val processingEnvironmentWrapper: ProcessingEnvironmentWrapper
) : SymbolProcessor {
    private val mLogger = Logger(logger)

    private lateinit var workers: Set<Worker<*, KSNode>>

    private val mCodeGenerator = KSPCodeGenerator(codeGenerator)

    data class CachedWorkSet(
        val allEntityElements: HashSet<KSAnnotated> = hashSetOf(),
        val allWrapperElements: HashSet<KSAnnotated> = hashSetOf(),
        val allSchemaClassElements: HashSet<KSAnnotated> = hashSetOf(),
        val allBaseModelElements: HashSet<KSAnnotated> = hashSetOf(),
        val allTypeConverterElements: HashSet<KSAnnotated> = hashSetOf(),
        val allTypeConverterExporterElements: HashSet<KSAnnotated> = hashSetOf(),
        val allTypeConverterImporterElements: HashSet<KSAnnotated> = hashSetOf(),
        val allMapperElements: HashSet<KSAnnotated> = hashSetOf()
    )

    private val cachedPreWorkset = CachedWorkSet()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        ProcessingContext.resolver = resolver
        ProcessingContext.logger = mLogger

        resolver.getSymbolsWithAnnotation(Entity::class.qualifiedName!!)
            .addProcessingTypes("Entity")
            .forEach {
                cachedPreWorkset.allEntityElements.add(it)
            }

        resolver.getSymbolsWithAnnotation(MapWrapper::class.qualifiedName!!)
            .addProcessingTypes("Wrapper")
            .forEach {
                cachedPreWorkset.allWrapperElements.add(it)
            }

        resolver.getSymbolsWithAnnotation(SchemaClass::class.qualifiedName!!).forEach {
            cachedPreWorkset.allSchemaClassElements.add(it)
        }

        resolver.getSymbolsWithAnnotation(BaseModel::class.qualifiedName!!).forEach {
            cachedPreWorkset.allBaseModelElements.add(it)
        }

        resolver.getSymbolsWithAnnotation(TypeConverter::class.qualifiedName!!).forEach {
            cachedPreWorkset.allTypeConverterElements.add(it)
        }

        resolver.getSymbolsWithAnnotation(TypeConverterExporter::class.qualifiedName!!).forEach {
            cachedPreWorkset.allTypeConverterExporterElements.add(it)
        }

        resolver.getSymbolsWithAnnotation(TypeConverterImporter::class.qualifiedName!!).forEach {
            cachedPreWorkset.allTypeConverterImporterElements.add(it)
        }

        resolver.getSymbolsWithAnnotation(Mapper::class.qualifiedName!!).forEach {
            cachedPreWorkset.allMapperElements.add(it)
        }

        return emptyList()
    }

    override fun finish() {
        workers =
            setOf(
                ModelWorker<KSNode>(
                    mLogger,
                    mCodeGenerator,
                    processingEnvironmentWrapper,
                    ModelWorkSet(
                        allEntityElements = cachedPreWorkset.allEntityElements.toSourceModel(),
                        allWrapperElements = cachedPreWorkset.allWrapperElements.toSourceModel(),
                        allSchemaClassElements = cachedPreWorkset.allSchemaClassElements.toSourceModel(),
                        allBaseModelElements = cachedPreWorkset.allBaseModelElements.toSourceModel(),
                        allTypeConverterElements = cachedPreWorkset.allTypeConverterElements.toSourceModel(),
                        allTypeConverterExporterElements = cachedPreWorkset.allTypeConverterExporterElements.toSourceModel(),
                        allTypeConverterImporterElements = cachedPreWorkset.allTypeConverterImporterElements.toSourceModel()
                    )
                ),
                MapperWorker(
                    mLogger,
                    mCodeGenerator,
                    processingEnvironmentWrapper,
                    MapperWorkSet(
                        allMapperElements = cachedPreWorkset.allMapperElements.toMapperSourceModel(),
                        PreMapperValidation::validate
                    )
                )
            )

        workers.forEach { it.init() }

        for (worker in workers) {
            try {
                if (!worker.invoke(processingEnvironmentWrapper.useSuspend == true)) {
                    // error in worker no further processing
                    return
                }
            } catch (e: PostValidationException) {
                mLogger.abortWithError(e.message ?: "", unboxError(e.causingElements), e)
                return
            }
        }
    }

    private fun unboxError(value: Any?): List<KSNode> {
        return when (value) {
            is List<*> -> value.map { it as KSNode }
            is KSNode -> listOf(value as KSNode)
            else -> emptyList()
        }
    }

    private fun Sequence<KSAnnotated>.addProcessingTypes(suffix: String): Sequence<KSAnnotated> {
        forEach {
            val clazz = it as KSClassDeclaration
            val className =
                com.squareup.kotlinpoet.ClassName(
                    clazz.packageName.asString(),
                    clazz.simpleName.asString() + suffix
                )
            if (ProcessingContext.processingTypes.contains(clazz.simpleName.asString() + suffix) && className.toString() != ProcessingContext.processingTypes[clazz.simpleName.asString() + suffix].toString()) {
                logger.error(
                    "Duplicate $suffix class found: ${clazz.simpleName.asString()} found in ${clazz.packageName.asString()}, ${ProcessingContext.processingTypes[clazz.simpleName.asString() + suffix]}"
                )
            }
            ProcessingContext.processingTypes[clazz.simpleName.asString() + suffix] = className
        }
        return this
    }

    private fun Set<KSAnnotated>.toSourceModel(): Set<ISourceModel<KSNode>> {
        return map {
            SourceModel(it as KSClassDeclaration)
        }.toSet()
    }

    private fun Set<KSAnnotated>.toMapperSourceModel(): Set<ISourceMapperModel<KSNode>> {
        return map { SourceMapperModel(it as KSClassDeclaration) }.toSet()
    }

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
}

class CrystalProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        environment.options
        return CrystalProcessor(
            environment.codeGenerator,
            environment.logger,
            ProcessingEnvironmentWrapper(environment.options)
        )
    }
}
