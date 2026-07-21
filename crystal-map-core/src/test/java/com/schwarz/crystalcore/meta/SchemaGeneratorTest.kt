package com.schwarz.crystalcore.meta

import com.schwarz.crystalapi.schema.EntitySchema
import com.schwarz.crystalcore.documentation.TestEntityHolder
import com.schwarz.crystalcore.util.decodeJsonOrNull
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class SchemaGeneratorTest {
    @TempDir
    lateinit var tempDir: File

    private val schemaFile get() = File(tempDir, "schema.json")

    private fun generator() = SchemaGenerator(tempDir.absolutePath, "schema.json")

    private fun source(name: String): String = File(tempDir, name).apply { writeText("class $name") }.absolutePath

    private fun schemaNames(): List<String> = decodeJsonOrNull<List<EntitySchema>>(schemaFile.readText())!!.map { it.name }

    @Test
    fun `merges the current run over the previous schema`() {
        generator().apply {
            addEntity(TestEntityHolder("Alpha"), listOf(source("Alpha.kt")))
            generate(mergeWithPrevious = true)
        }

        val betaSource = source("Beta.kt")
        generator().apply {
            addEntity(TestEntityHolder("Beta"), listOf(betaSource))
            generate(mergeWithPrevious = true, reprocessedFilePaths = setOf(betaSource))
        }

        assertEquals(listOf("Alpha", "Beta"), schemaNames())
    }

    @Test
    fun `keeps an undecodable schema file unchanged instead of degrading it`() {
        schemaFile.writeText("{ not a schema")

        generator().apply {
            addEntity(TestEntityHolder("Alpha"), listOf(source("Alpha.kt")))
            generate(mergeWithPrevious = true)
        }

        assertEquals("{ not a schema", schemaFile.readText())
    }

    @Test
    fun `regenerates a deleted schema file from the sidecar`() {
        generator().apply {
            addEntity(TestEntityHolder("Alpha"), listOf(source("Alpha.kt")))
            addEntity(TestEntityHolder("Beta"), listOf(source("Beta.kt")))
            generate(mergeWithPrevious = true)
        }
        schemaFile.delete()

        generator().generate(mergeWithPrevious = true)

        assertEquals(listOf("Alpha", "Beta"), schemaNames())
    }

    @Test
    fun `repairs a corrupt schema file from the sidecar`() {
        generator().apply {
            addEntity(TestEntityHolder("Alpha"), listOf(source("Alpha.kt")))
            generate(mergeWithPrevious = true)
        }
        schemaFile.writeText("{ damaged")

        generator().generate(mergeWithPrevious = true)

        assertEquals(listOf("Alpha"), schemaNames())
    }

    @Test
    fun `migrates a legacy sources-only sidecar using the schema file as model`() {
        val alphaSource = source("Alpha.kt")
        generator().apply {
            addEntity(TestEntityHolder("Alpha"), listOf(alphaSource))
            generate(mergeWithPrevious = true)
        }
        val modelFile = File(tempDir, "schema.json.model")
        modelFile.writeText(Json.encodeToString(mapOf("Alpha" to listOf(alphaSource))))

        val betaSource = source("Beta.kt")
        generator().apply {
            addEntity(TestEntityHolder("Beta"), listOf(betaSource))
            generate(mergeWithPrevious = true, reprocessedFilePaths = setOf(betaSource))
        }

        assertEquals(listOf("Alpha", "Beta"), schemaNames())
        assertNotNull(
            decodeJsonOrNull<SchemaGenerator.SchemaModel>(modelFile.readText()),
            "legacy sidecar must be upgraded to the full model format",
        )
    }

    @Test
    fun `rebuilds an absent schema from the current run`() {
        generator().apply {
            addEntity(TestEntityHolder("Alpha"))
            generate(mergeWithPrevious = true)
        }

        assertNotNull(decodeJsonOrNull<List<EntitySchema>>(schemaFile.readText()))
        assertEquals(listOf("Alpha"), schemaNames())
    }
}
