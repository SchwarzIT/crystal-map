package com.schwarz.crystalcore.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.attribute.FileTime
import java.nio.file.attribute.PosixFilePermission

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

    @Test
    fun `overload with cached previous content skips write without re-reading`() {
        val file = File(tempDir, "output.txt")
        file.writeText("content")
        val oldTimestamp = FileTime.fromMillis(1_000_000L)
        Files.setLastModifiedTime(file.toPath(), oldTimestamp)

        file.writeTextIfChanged("content", "content")

        assertEquals(oldTimestamp, Files.getLastModifiedTime(file.toPath()))
    }

    @Test
    fun `overload with null previous content writes`() {
        val file = File(tempDir, "output.txt")

        file.writeTextIfChanged("content", null)

        assertEquals("content", file.readText())
    }

    @Test
    fun `atomic write leaves no temp files behind`() {
        val file = File(tempDir, "output.txt")
        file.writeText("old")

        file.writeTextIfChanged("new")

        assertEquals(listOf("output.txt"), tempDir.list()!!.toList())
    }

    @Test
    fun `readTextOrNull returns content or null`() {
        val file = File(tempDir, "output.txt")
        assertEquals(null, file.readTextOrNull())

        file.writeText("content")
        assertEquals("content", file.readTextOrNull())
    }

    @Test
    fun `decodeJsonOrNull decodes valid json and swallows garbage`() {
        assertEquals(mapOf("a" to "b"), decodeJsonOrNull<Map<String, String>>("""{"a":"b"}"""))
        assertEquals(null, decodeJsonOrNull<Map<String, String>>("not json"))
        assertEquals(null, decodeJsonOrNull<Map<String, String>>(null))
    }

    @Test
    fun `atomically written files stay readable beyond the owner`() {
        Assumptions.assumeTrue(
            FileSystems.getDefault().supportedFileAttributeViews().contains("posix"),
        )
        val file = File(tempDir, "output.txt")

        file.writeTextIfChanged("content")

        val permissions = Files.getPosixFilePermissions(file.toPath())
        assertTrue(
            PosixFilePermission.GROUP_READ in permissions || PosixFilePermission.OTHERS_READ in permissions,
            "side outputs must not end up owner-only: $permissions",
        )
    }
}
