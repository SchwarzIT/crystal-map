package com.schwarz.crystalcore.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.FileTime

class FileUtilTest {
    @TempDir
    lateinit var tempDir: File

    @Test
    fun `writes file when it does not exist`() {
        val file = File(tempDir, "output.txt")

        file.writeTextIfChanged("content")

        assertTrue(file.exists())
        assertEquals("content", file.readText())
    }

    @Test
    fun `skips write when content is identical`() {
        val file = File(tempDir, "output.txt")
        file.writeText("content")
        val oldTimestamp = FileTime.fromMillis(1_000_000L)
        Files.setLastModifiedTime(file.toPath(), oldTimestamp)

        file.writeTextIfChanged("content")

        assertEquals(oldTimestamp, Files.getLastModifiedTime(file.toPath()))
        assertEquals("content", file.readText())
    }

    @Test
    fun `rewrites file when content differs`() {
        val file = File(tempDir, "output.txt")
        file.writeText("old content")
        val oldTimestamp = FileTime.fromMillis(1_000_000L)
        Files.setLastModifiedTime(file.toPath(), oldTimestamp)

        file.writeTextIfChanged("new content")

        assertEquals("new content", file.readText())
        assertNotEquals(oldTimestamp, Files.getLastModifiedTime(file.toPath()))
    }
}
