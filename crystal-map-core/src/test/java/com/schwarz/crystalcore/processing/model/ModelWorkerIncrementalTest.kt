package com.schwarz.crystalcore.processing.model

import com.schwarz.crystalcore.ICodeGenerator
import com.schwarz.crystalcore.ILogger
import com.schwarz.crystalcore.ISettings
import com.schwarz.crystalcore.documentation.FakeSourceModel
import com.schwarz.crystalcore.model.accessor.CblGenerateAccessorHolder
import com.schwarz.crystalcore.model.source.ISourceMapWrapper
import com.schwarz.crystalcore.model.source.ISourceModel
import com.squareup.kotlinpoet.FileSpec
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * The work set can span the whole compilation (world knowledge for validation
 * and type resolution of an incremental run), but only models originating from
 * the regeneration file set may produce output files: KSP deletes only the
 * outputs of dirty sources, and creating an existing file fails the build.
 */
class ModelWorkerIncrementalTest {
    @Test
    fun `incremental run generates only models from regeneration file paths`() {
        val generator = RecordingCodeGenerator()
        val worker =
            modelWorker(
                generator,
                incremental = true,
                regenerationFilePaths = setOf("Dirty.kt"),
            )

        worker.init()
        assertTrue(worker.invoke(false))

        assertTrue(
            generator.generatedFileNames.isNotEmpty(),
            "expected output for the reprocessed wrapper",
        )
        assertTrue(
            generator.generatedFileNames.all { it.contains("Dirty") },
            "expected only Dirty* outputs, got: ${generator.generatedFileNames}",
        )
        assertFalse(
            generator.generatedFileNames.any { it.contains("Clean") },
            "untouched sources must not be regenerated, got: ${generator.generatedFileNames}",
        )
    }

    @Test
    fun `full run generates all models`() {
        val generator = RecordingCodeGenerator()
        val worker = modelWorker(generator, incremental = false, regenerationFilePaths = emptySet())

        worker.init()
        assertTrue(worker.invoke(false))

        assertTrue(
            generator.generatedFileNames.any { it.contains("Dirty") },
            "expected Dirty* outputs, got: ${generator.generatedFileNames}",
        )
        assertTrue(
            generator.generatedFileNames.any { it.contains("Clean") },
            "expected Clean* outputs, got: ${generator.generatedFileNames}",
        )
    }

    private fun modelWorker(
        generator: RecordingCodeGenerator,
        incremental: Boolean,
        regenerationFilePaths: Set<String>,
    ): ModelWorker<Unit> =
        ModelWorker(
            NoOpLogger(),
            generator,
            EmptySettings(),
            ModelWorkSet(
                allEntityElements = emptySet(),
                allWrapperElements =
                    setOf<ISourceModel<Unit>>(
                        FakeSourceModel(
                            "Dirty",
                            originatingFiles = listOf("Dirty.kt"),
                            mapWrapperAnnotation = OpenMapWrapper,
                        ),
                        FakeSourceModel(
                            "Clean",
                            originatingFiles = listOf("Clean.kt"),
                            mapWrapperAnnotation = OpenMapWrapper,
                        ),
                    ),
                allSchemaClassElements = emptySet(),
                allBaseModelElements = emptySet(),
                allTypeConverterElements = emptySet(),
                allTypeConverterExporterElements = emptySet(),
                allTypeConverterImporterElements = emptySet(),
            ),
            incremental = incremental,
            regenerationFilePaths = regenerationFilePaths,
            originFilePath = { it as? String },
        )

    private object OpenMapWrapper : ISourceMapWrapper {
        override val modifierOpen: Boolean = true
    }

    private class RecordingCodeGenerator : ICodeGenerator {
        val generatedFileNames = mutableListOf<String>()

        override fun generate(
            entityToGenerate: FileSpec,
            settings: ISettings,
        ) {
            generatedFileNames.add(entityToGenerate.name)
        }

        override fun generateAndFixAccessors(
            entityToGenerate: FileSpec,
            generateAccessors: MutableList<CblGenerateAccessorHolder>,
            settings: ISettings,
        ) {
            generatedFileNames.add(entityToGenerate.name)
        }
    }

    private class EmptySettings : ISettings {
        override val kotlinGeneratedPath: String? = null
        override val documentationPath: String? = null
        override val documentationFilename: String? = null
        override val schemaPath: String? = null
        override val schemaFilename: String? = null
        override val entityRelationshipPath: String? = null
        override val entityRelationshipFilename: String? = null
    }

    private class NoOpLogger : ILogger<Unit> {
        private var errors = false

        override fun info(msg: String) = Unit

        override fun info(
            msg: String,
            element: Unit?,
        ) = Unit

        override fun warn(
            msg: String,
            element: Unit?,
        ) = Unit

        override fun error(
            msg: String,
            element: Unit?,
        ) {
            errors = true
            throw AssertionError("unexpected processor error: $msg")
        }

        override fun abortWithError(
            msg: String,
            element: Unit?,
            ex: Throwable?,
        ): Unit = throw AssertionError("unexpected abort: $msg")

        override fun abortWithError(
            msg: String?,
            elements: List<Unit>,
            ex: Throwable?,
        ): Unit = throw AssertionError("unexpected abort: $msg")

        override fun hasErrors(): Boolean = errors
    }
}
