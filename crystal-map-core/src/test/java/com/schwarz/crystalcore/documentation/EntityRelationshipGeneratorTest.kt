package com.schwarz.crystalcore.documentation

import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class EntityRelationshipGeneratorTest {
    @TempDir
    lateinit var tempDir: File

    private val graphFile get() = File(tempDir, "er.gv")
    private val modelFile get() = File(tempDir, "er.gv.model")

    private fun generator() = EntityRelationshipGenerator(tempDir.absolutePath, "er.gv")

    private fun source(name: String): String = File(tempDir, name).apply { writeText("class $name") }.absolutePath

    @Test
    fun `drops carried-over edges to entities purged in this run`() {
        val alphaSource = source("Alpha.kt")
        modelFile.writeText(
            Json.encodeToString(
                EntityRelationshipGenerator.RelationshipModel(
                    nodes = mapOf("Alpha" to "<table>alpha</table>", "Beta" to "<table>beta</table>"),
                    edges = mapOf("Alpha" to listOf("Beta")),
                    sources =
                        mapOf(
                            "Alpha" to listOf(alphaSource),
                            "Beta" to listOf(File(tempDir, "Deleted.kt").absolutePath),
                        ),
                ),
            ),
        )

        generator().generate(mergeWithPrevious = true)

        val graph = graphFile.readText()
        assertTrue(graph.contains("Alpha [label=<"))
        assertFalse(graph.contains("Beta"), "purged entity must not linger as a phantom edge target")
    }

    @Test
    fun `keeps unseen entities of a pre-sidecar graph by reconstructing them`() {
        generator().apply {
            addEntityNodes(TestEntityHolder("Alpha"))
            addEntityNodes(TestEntityHolder("Beta"))
            generate()
        }
        modelFile.delete()

        val gammaSource = source("Gamma.kt")
        generator().apply {
            addEntityNodes(TestEntityHolder("Gamma"), listOf(gammaSource))
            generate(mergeWithPrevious = true, reprocessedFilePaths = setOf(gammaSource))
        }

        val graph = graphFile.readText()
        listOf("Alpha", "Beta", "Gamma").forEach {
            assertTrue(graph.contains("$it [label=<"), "expected node $it in graph")
        }
        assertTrue(modelFile.exists(), "expected the sidecar to be restored")
    }

    @Test
    fun `repairs a damaged output file even when the model is unchanged`() {
        generator().apply {
            addEntityNodes(TestEntityHolder("Alpha"), listOf(source("Alpha.kt")))
            generate(mergeWithPrevious = true)
        }
        graphFile.writeText("damaged")

        generator().generate(mergeWithPrevious = true)

        assertTrue(graphFile.readText().contains("Alpha [label=<"), "damaged output must be re-rendered")
    }
}
