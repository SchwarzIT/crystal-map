package com.schwarz.crystalksp

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertTimeoutPreemptively
import java.time.Duration

/**
 * Termination and single-visit guarantees of the referenced-world closure:
 * cyclic or self-referencing models must not loop, and every name must be
 * expanded at most once.
 */
class ExpandReferencesTest {
    @Test
    fun `cyclic references terminate and expand each name exactly once`() {
        val graph =
            mapOf(
                "A" to listOf("B"),
                "B" to listOf("A"),
            )
        val expanded = mutableListOf<String>()
        val added = mutableListOf<String>()

        assertTimeoutPreemptively(Duration.ofSeconds(10)) {
            expandReferences(
                seeds = setOf("A"),
                referencedTypeNames = { name ->
                    expanded.add(name)
                    graph[name] ?: emptyList()
                },
                addReferenced = { name ->
                    added.add(name)
                    true
                },
            )
        }

        assertEquals(listOf("A", "B"), expanded)
        // A is a seed and must never be re-added when B references it back.
        assertEquals(listOf("B"), added)
    }

    @Test
    fun `self reference terminates without re-adding the class`() {
        val expanded = mutableListOf<String>()
        val added = mutableListOf<String>()

        assertTimeoutPreemptively(Duration.ofSeconds(10)) {
            expandReferences(
                seeds = setOf("A"),
                referencedTypeNames = { name ->
                    expanded.add(name)
                    listOf("A")
                },
                addReferenced = { name ->
                    added.add(name)
                    true
                },
            )
        }

        assertEquals(listOf("A"), expanded)
        assertTrue(added.isEmpty(), "a seed referencing itself must not be re-added, got: $added")
    }

    @Test
    fun `shared reference from multiple seeds is expanded once`() {
        val graph =
            mapOf(
                "A" to listOf("C"),
                "B" to listOf("C"),
                "C" to listOf("A", "B"),
            )
        val expanded = mutableListOf<String>()
        val added = mutableListOf<String>()

        expandReferences(
            seeds = setOf("A", "B"),
            referencedTypeNames = { name ->
                expanded.add(name)
                graph[name] ?: emptyList()
            },
            addReferenced = { name ->
                added.add(name)
                true
            },
        )

        assertEquals(listOf("A", "B", "C"), expanded)
        assertEquals(listOf("C"), added)
    }

    @Test
    fun `references rejected by addReferenced are not expanded`() {
        val graph =
            mapOf(
                "A" to listOf("X"),
                "X" to listOf("Y"),
            )
        val expanded = mutableListOf<String>()

        expandReferences(
            seeds = setOf("A"),
            referencedTypeNames = { name ->
                expanded.add(name)
                graph[name] ?: emptyList()
            },
            addReferenced = { false },
        )

        assertEquals(listOf("A"), expanded)
    }
}
