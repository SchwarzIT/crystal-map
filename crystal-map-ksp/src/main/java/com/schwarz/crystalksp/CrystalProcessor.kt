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
import com.schwarz.crystalksp.model.source.SourceMapperModel
import com.schwarz.crystalksp.validation.mapper.PreMapperValidation
import com.schwarz.crystalksp.generation.KSPCodeGenerator
import com.schwarz.crystalksp.model.source.SourceModel
import kotlin.metadata.ClassName

class CrystalProcessor(codeGenerator: CodeGenerator, val logger: KSPLogger, val processingEnvironmentWrapper: ProcessingEnvironmentWrapper) : SymbolProcessor {

//    private val shadowBeanGenerator = ShadowBeanGenerator()
//
//    private val factoryGenerator = FactoryGenerator()
//
//    private val codeGenerator = KokainCodeGenerator(codeGenerator)

    private val mLogger = Logger(logger)

    private lateinit var workers: Set<Worker<*, KSNode>>

    private val mCodeGenerator = KSPCodeGenerator(codeGenerator)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        ProcessingContext.resolver = resolver
        ProcessingContext.logger = mLogger

        workers = setOf(
            ModelWorker<KSNode>(
                mLogger,
                mCodeGenerator,
                processingEnvironmentWrapper,
                ModelWorkSet(
                    allEntityElements = resolver.getSymbolsWithAnnotation(Entity::class.qualifiedName!!)
                        .toEntitySourceModel(),
                    allWrapperElements = resolver.getSymbolsWithAnnotation(MapWrapper::class.qualifiedName!!)
                        .toSourceModel(),
                    allSchemaClassElements = resolver.getSymbolsWithAnnotation(SchemaClass::class.qualifiedName!!)
                        .toSourceModel(),
                    allBaseModelElements = resolver.getSymbolsWithAnnotation(BaseModel::class.qualifiedName!!)
                        .toSourceModel(),
                    allTypeConverterElements = resolver.getSymbolsWithAnnotation(TypeConverter::class.qualifiedName!!)
                        .toSourceModel(),
                    allTypeConverterExporterElements = resolver.getSymbolsWithAnnotation(
                        TypeConverterExporter::class.qualifiedName!!
                    ).toSourceModel(),
                    allTypeConverterImporterElements = resolver.getSymbolsWithAnnotation(
                        TypeConverterImporter::class.qualifiedName!!
                    ).toSourceModel()
                )
            ),
            MapperWorker(
                mLogger,
                mCodeGenerator,
                processingEnvironmentWrapper,
                MapperWorkSet(
                    allMapperElements = resolver.getSymbolsWithAnnotation(Mapper::class.qualifiedName!!).toMapperSourceModel(),
                    PreMapperValidation::validate
                )
            )
        )

        workers.forEach { it.init() }

        for (worker in workers) {
            try {
                if (!worker.invoke(processingEnvironmentWrapper.useSuspend == true)) {
                    // error in worker no further processing
                    return emptyList()
                }
            } catch (e: PostValidationException) {
                mLogger.abortWithError(e.message ?: "", unboxError(e.causingElements), e)
                return emptyList()
            }
        }
        return emptyList()
    }

    private fun unboxError(value: Any?): List<KSNode> {
        return when (value) {
            is List<*> -> value.map { it as KSNode }
            is KSNode -> listOf(value as KSNode)
            else -> emptyList()
        }
    }

    private fun Sequence<KSAnnotated>.toEntitySourceModel(): Set<ISourceModel<KSNode>> {
        return map {
            val clazz = it as KSClassDeclaration
            ProcessingContext.processingTypes[clazz.simpleName.asString() + "Entity"] = com.squareup.kotlinpoet.ClassName(clazz.packageName.asString(), clazz.simpleName.asString() + "Entity")
            SourceModel(it as KSClassDeclaration)
        }.toSet()
    }

    private fun Sequence<KSAnnotated>.toSourceModel(): Set<ISourceModel<KSNode>> {
        return map { SourceModel(it as KSClassDeclaration) }.toSet()
    }

    private fun Sequence<KSAnnotated>.toMapperSourceModel(): Set<ISourceMapperModel<KSNode>> {
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
    override fun create(
        environment: SymbolProcessorEnvironment
    ): SymbolProcessor {
        environment.options
        return CrystalProcessor(environment.codeGenerator, environment.logger, ProcessingEnvironmentWrapper(environment.options))
    }
}
