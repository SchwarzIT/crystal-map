package com.schwarz.crystalcore.meta

import com.schwarz.crystalapi.schema.EntitySchema
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SchemaGeneratorTest {
    @Test
    fun `merge keeps entities that were not part of the current run`() {
        val previous = listOf(schema("Product"), schema("UserComment"))
        val current = mapOf("Article" to schema("Article"))

        val merged = SchemaGenerator.merge(previous, current)

        assertEquals(listOf("Article", "Product", "UserComment"), merged.map { it.name })
    }

    @Test
    fun `merge lets the current run win over previous content`() {
        val previous = listOf(schema("Product", basedOn = listOf("Old")))
        val current = mapOf("Product" to schema("Product", basedOn = listOf("New")))

        val merged = SchemaGenerator.merge(previous, current)

        assertEquals(1, merged.size)
        assertEquals(listOf("New"), merged.single().basedOn)
    }

    @Test
    fun `merge of empty previous returns current sorted by name`() {
        val current = mapOf("B" to schema("B"), "A" to schema("A"))

        val merged = SchemaGenerator.merge(emptyList(), current)

        assertEquals(listOf("A", "B"), merged.map { it.name })
    }

    private fun schema(
        name: String,
        basedOn: List<String> = emptyList(),
    ): EntitySchema =
        EntitySchema(
            name = name,
            fields = emptyList(),
            basedOn = basedOn,
            queries = emptyList(),
            docId = null,
            deprecatedSchema = null,
        )
}
