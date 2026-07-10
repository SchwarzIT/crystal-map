package com.schwarz.crystalcore.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class SideOutputMergeTest {
    @TempDir
    lateinit var tempDir: File

    private fun source(name: String): String = File(tempDir, name).apply { writeText("class $name") }.absolutePath

    @Test
    fun `keeps previous entries whose sources still exist and were not reprocessed`() {
        val productSource = source("Product.kt")

        val merged =
            mergeSideOutputEntries(
                previousEntries = mapOf("Product" to "old"),
                previousSources = mapOf("Product" to listOf(productSource)),
                currentEntries = mapOf("Article" to "new"),
                reprocessedFilePaths = setOf(source("Article.kt")),
            )

        assertEquals(mapOf("Article" to "new", "Product" to "old"), merged)
    }

    @Test
    fun `current run wins over previous entry of the same name`() {
        val merged =
            mergeSideOutputEntries(
                previousEntries = mapOf("Product" to "old"),
                previousSources = mapOf("Product" to listOf(source("Product.kt"))),
                currentEntries = mapOf("Product" to "new"),
                reprocessedFilePaths = emptySet(),
            )

        assertEquals(mapOf("Product" to "new"), merged)
    }

    @Test
    fun `purges previous entry when all of its source files were deleted`() {
        val merged =
            mergeSideOutputEntries(
                previousEntries = mapOf("Deleted" to "old", "Kept" to "old"),
                previousSources =
                    mapOf(
                        "Deleted" to listOf(File(tempDir, "Gone.kt").absolutePath),
                        "Kept" to listOf(source("Kept.kt")),
                    ),
                currentEntries = emptyMap(),
                reprocessedFilePaths = emptySet(),
            )

        assertEquals(mapOf("Kept" to "old"), merged)
    }

    @Test
    fun `purges previous entry whose source was reprocessed without producing it again`() {
        val annotationRemoved = source("AnnotationRemoved.kt")

        val merged =
            mergeSideOutputEntries(
                previousEntries = mapOf("AnnotationRemoved" to "old", "Kept" to "old"),
                previousSources =
                    mapOf(
                        "AnnotationRemoved" to listOf(annotationRemoved),
                        "Kept" to listOf(source("Kept.kt")),
                    ),
                currentEntries = emptyMap(),
                reprocessedFilePaths = setOf(annotationRemoved),
            )

        assertEquals(mapOf("Kept" to "old"), merged)
    }

    @Test
    fun `keeps previous entry without source information conservatively`() {
        val merged =
            mergeSideOutputEntries(
                previousEntries = mapOf("Unknown" to "old"),
                previousSources = emptyMap(),
                currentEntries = emptyMap(),
                reprocessedFilePaths = setOf("whatever"),
            )

        assertEquals(mapOf("Unknown" to "old"), merged)
    }

    @Test
    fun `result is sorted by entry name`() {
        val merged =
            mergeSideOutputEntries(
                previousEntries = mapOf("Zebra" to "z"),
                previousSources = mapOf("Zebra" to listOf(source("Zebra.kt"))),
                currentEntries = mapOf("Alpha" to "a", "Mid" to "m"),
                reprocessedFilePaths = emptySet(),
            )

        assertEquals(listOf("Alpha", "Mid", "Zebra"), merged.keys.toList())
    }
}
