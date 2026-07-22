package com.schwarz.crystalksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSNode
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.io.OutputStream
import java.lang.reflect.Proxy

/**
 * An exception escaping the processor poisons KSP2's daemon-held lookup
 * caches ("Storage for [...] is already registered" on every following build
 * until the daemon is stopped, google/ksp#2134). Crashes must therefore be
 * converted into reported errors instead of propagating.
 */
class CrystalProcessorCrashHandlingTest {
    @Test
    fun `crash during round collection is reported instead of thrown`() {
        val logger = RecordingLogger()
        val processor = processor(logger, options = emptyMap())
        val throwingResolver =
            Proxy.newProxyInstance(
                Resolver::class.java.classLoader,
                arrayOf(Resolver::class.java),
            ) { _, _, _ -> throw IllegalStateException("boom") } as Resolver

        processor.process(throwingResolver)

        assertTrue(
            logger.errors.any { it.contains("crystal-map processor crashed") },
            "expected a reported crash, got: ${logger.errors}",
        )
    }

    @Test
    fun `crash during finish is reported instead of thrown`(
        @TempDir tempDir: File,
    ) {
        // A plain file as parent makes the registry directory uncreatable, so
        // persisting the registry in finish() throws.
        val blocker = File(tempDir, "blocker").apply { writeText("") }
        val logger = RecordingLogger()
        val processor =
            processor(
                logger,
                options =
                    mapOf(
                        CrystalProcessor.FRAMEWORK_INCREMENTAL_REGISTRY_PATH_OPTION_NAME to
                            File(blocker, "registry").absolutePath,
                    ),
            )

        processor.finish()

        assertTrue(
            logger.errors.any { it.contains("crystal-map processor crashed") },
            "expected a reported crash, got: ${logger.errors}",
        )
    }

    private fun processor(
        logger: KSPLogger,
        options: Map<String, String>,
    ): CrystalProcessor =
        CrystalProcessor(
            NoOpCodeGenerator(),
            logger,
            ProcessingEnvironmentWrapper(options),
        )

    private class RecordingLogger : KSPLogger {
        val errors = mutableListOf<String>()

        override fun logging(
            message: String,
            symbol: KSNode?,
        ) = Unit

        override fun info(
            message: String,
            symbol: KSNode?,
        ) = Unit

        override fun warn(
            message: String,
            symbol: KSNode?,
        ) = Unit

        override fun error(
            message: String,
            symbol: KSNode?,
        ) {
            errors.add(message)
        }

        override fun exception(e: Throwable) {
            errors.add(e.toString())
        }
    }

    private class NoOpCodeGenerator : CodeGenerator {
        override val generatedFile: Collection<File> = emptyList()

        override fun createNewFile(
            dependencies: Dependencies,
            packageName: String,
            fileName: String,
            extensionName: String,
        ): OutputStream = OutputStream.nullOutputStream()

        override fun createNewFileByPath(
            dependencies: Dependencies,
            path: String,
            extensionName: String,
        ): OutputStream = OutputStream.nullOutputStream()

        override fun associate(
            sources: List<KSFile>,
            packageName: String,
            fileName: String,
            extensionName: String,
        ) = Unit

        override fun associateByPath(
            sources: List<KSFile>,
            path: String,
            extensionName: String,
        ) = Unit

        override fun associateWithClasses(
            classes: List<KSClassDeclaration>,
            packageName: String,
            fileName: String,
            extensionName: String,
        ) = Unit
    }
}
