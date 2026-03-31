package com.schwarz.crystalksp.generation

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class GenerationCacheTest {
    @TempDir
    lateinit var tempDir: File

    @Test
    fun testEmptyCacheAlwaysReturnsTrue() {
        val cache = GenerationCache(File(tempDir, "cache.tsv"))
        assertTrue(cache.shouldGenerate("com.example", "Foo", "content"))
        assertTrue(cache.shouldGenerate("com.example", "Bar", "other content"))
    }

    @Test
    fun testCachedHashSkipsGeneration() {
        val cacheFile = File(tempDir, "cache.tsv")
        val content = "package com.example\nclass Foo"

        // First run: generate and save
        val cache1 = GenerationCache(cacheFile)
        assertTrue(cache1.shouldGenerate("com.example", "Foo", content))
        cache1.save()

        // Second run: same content should be skipped
        val cache2 = GenerationCache(cacheFile)
        assertFalse(cache2.shouldGenerate("com.example", "Foo", content))
    }

    @Test
    fun testChangedContentTriggersGeneration() {
        val cacheFile = File(tempDir, "cache.tsv")

        // First run
        val cache1 = GenerationCache(cacheFile)
        cache1.shouldGenerate("com.example", "Foo", "original content")
        cache1.save()

        // Second run: different content
        val cache2 = GenerationCache(cacheFile)
        assertTrue(cache2.shouldGenerate("com.example", "Foo", "modified content"))
    }

    @Test
    fun testSaveAndLoadRoundtrip() {
        val cacheFile = File(tempDir, "cache.tsv")

        val cache1 = GenerationCache(cacheFile)
        cache1.shouldGenerate("com.a", "FileA", "contentA")
        cache1.shouldGenerate("com.b", "FileB", "contentB")
        cache1.shouldGenerate("com.c", "FileC", "contentC")
        cache1.save()

        val cache2 = GenerationCache(cacheFile)
        assertFalse(cache2.shouldGenerate("com.a", "FileA", "contentA"))
        assertFalse(cache2.shouldGenerate("com.b", "FileB", "contentB"))
        assertFalse(cache2.shouldGenerate("com.c", "FileC", "contentC"))
        assertTrue(cache2.shouldGenerate("com.d", "FileD", "contentD"))
    }

    @Test
    fun testMissingCacheFileActsAsEmpty() {
        val cacheFile = File(tempDir, "nonexistent.tsv")
        assertFalse(cacheFile.exists())

        val cache = GenerationCache(cacheFile)
        assertTrue(cache.shouldGenerate("com.example", "Foo", "content"))
    }

    @Test
    fun testSaveWithEmptyCurrentHashesPreservesExistingCache() {
        val cacheFile = File(tempDir, "cache.tsv")

        // First run: generate and save
        val cache1 = GenerationCache(cacheFile)
        cache1.shouldGenerate("com.example", "Foo", "content")
        cache1.save()
        assertTrue(cacheFile.exists())
        val savedContent = cacheFile.readText()
        assertTrue(savedContent.isNotEmpty())

        // Second run: no shouldGenerate calls (simulates incremental run with no annotated changes)
        val cache2 = GenerationCache(cacheFile)
        cache2.save()

        // Cache file should still contain the original data
        val preservedContent = cacheFile.readText()
        assertTrue(preservedContent.isNotEmpty())
        assertTrue(preservedContent == savedContent)

        // Third run: previous hashes still work
        val cache3 = GenerationCache(cacheFile)
        assertFalse(cache3.shouldGenerate("com.example", "Foo", "content"))
    }

    @Test
    fun testSaveCreatesParentDirectories() {
        val cacheFile = File(tempDir, "sub/dir/cache.tsv")

        val cache = GenerationCache(cacheFile)
        cache.shouldGenerate("com.example", "Foo", "content")
        cache.save()

        assertTrue(cacheFile.exists())
    }
}
