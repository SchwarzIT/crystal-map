package com.schwarz.crystalcore.documentation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class DocumentationGeneratorTest {
    @TempDir
    lateinit var tempDir: File

    private val htmlFile get() = File(tempDir, "doc.html")
    private val modelFile get() = File(tempDir, "doc.html.model")

    private fun generator() = DocumentationGenerator(tempDir.absolutePath, "doc.html")

    private fun source(name: String): String = File(tempDir, name).apply { writeText("class $name") }.absolutePath

    @Test
    fun `keeps unseen entities of a pre-sidecar document by reconstructing them`() {
        generator().apply {
            addEntitySegments(TestEntityHolder("Alpha"))
            addEntitySegments(TestEntityHolder("Beta"))
            addEntitySegments(TestEntityHolder("Gamma"))
            generate()
        }
        modelFile.delete()

        generator().apply {
            addEntitySegments(TestEntityHolder("Delta"), listOf(source("Delta.kt")))
            generate(mergeWithPrevious = true, reprocessedFilePaths = setOf(source("Delta.kt")))
        }

        val document = htmlFile.readText()
        listOf("Alpha", "Beta", "Gamma", "Delta").forEach {
            assertTrue(document.contains("<h1>$it</h1>"), "expected $it in document")
        }
        assertTrue(modelFile.exists(), "expected the sidecar to be restored")
    }

    @Test
    fun `partial run on a pre-sidecar document does not freeze generation after entities were deleted`() {
        generator().apply {
            addEntitySegments(TestEntityHolder("Alpha"))
            addEntitySegments(TestEntityHolder("Beta"))
            generate()
        }
        modelFile.delete()

        val alphaSource = source("Alpha.kt")
        generator().apply {
            addEntitySegments(TestEntityHolder("Alpha"), listOf(alphaSource))
            generate(mergeWithPrevious = true, reprocessedFilePaths = setOf(alphaSource))
        }

        val document = htmlFile.readText()
        assertTrue(document.contains("<h1>Alpha</h1>"))
        assertTrue(document.contains("<h1>Beta</h1>"), "unseen entity must be carried over, not dropped")
        assertTrue(modelFile.exists(), "generation must not freeze without a sidecar")
    }

    @Test
    fun `repairs a damaged output file even when the model is unchanged`() {
        val alphaSource = source("Alpha.kt")
        generator().apply {
            addEntitySegments(TestEntityHolder("Alpha"), listOf(alphaSource))
            generate(mergeWithPrevious = true)
        }
        htmlFile.writeText("damaged")

        generator().generate(mergeWithPrevious = true)

        assertTrue(htmlFile.readText().contains("<h1>Alpha</h1>"), "damaged output must be re-rendered")
    }

    @Test
    fun `purges entities whose reprocessed source no longer produces them`() {
        val alphaSource = source("Alpha.kt")
        val betaSource = source("Beta.kt")
        generator().apply {
            addEntitySegments(TestEntityHolder("Alpha"), listOf(alphaSource))
            addEntitySegments(TestEntityHolder("Beta"), listOf(betaSource))
            generate(mergeWithPrevious = true)
        }

        generator().generate(mergeWithPrevious = true, reprocessedFilePaths = setOf(betaSource))

        val document = htmlFile.readText()
        assertTrue(document.contains("<h1>Alpha</h1>"))
        assertFalse(document.contains("<h1>Beta</h1>"), "entity of reprocessed source must be purged")
    }

    @Test
    fun `merge run without previous state produces the current model`() {
        generator().apply {
            addEntitySegments(TestEntityHolder("Alpha"))
            generate(mergeWithPrevious = true)
        }

        assertTrue(htmlFile.readText().contains("<h1>Alpha</h1>"))
        assertEquals(true, modelFile.exists())
    }
}
