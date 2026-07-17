package com.schwarz.crystalcore.meta

import com.schwarz.crystalapi.schema.EntitySchema
import com.schwarz.crystalcore.documentation.TestEntityHolder
import com.schwarz.crystalcore.util.decodeJsonOrNull
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
    fun `rebuilds an absent schema from the current run`() {
        generator().apply {
            addEntity(TestEntityHolder("Alpha"))
            generate(mergeWithPrevious = true)
        }

        assertNotNull(decodeJsonOrNull<List<EntitySchema>>(schemaFile.readText()))
        assertEquals(listOf("Alpha"), schemaNames())
    }
}
