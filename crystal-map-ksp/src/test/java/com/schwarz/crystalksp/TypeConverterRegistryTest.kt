package com.schwarz.crystalksp

import com.schwarz.crystalksp.TypeConverterRegistry.Entry
import com.schwarz.crystalksp.TypeConverterRegistry.Kind
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class TypeConverterRegistryTest {
    @TempDir
    lateinit var tempDir: File

    private val registryFile get() = File(tempDir, TypeConverterRegistry.FILE_NAME)

    @Test
    fun `save and load round-trip`() {
        val entries =
            listOf(
                Entry(Kind.CONVERTER, "com.example.DateConverter", "/src/DateConverter.kt"),
                Entry(Kind.IMPORTER, "com.example.Importer", "/src/Importer.kt"),
            )
        TypeConverterRegistry.save(registryFile, entries)
        assertEquals(entries.sortedBy { it.qualifiedName }, TypeConverterRegistry.load(registryFile).sortedBy { it.qualifiedName })
    }

    @Test
    fun `missing file loads as empty`() {
        assertTrue(TypeConverterRegistry.load(registryFile).isEmpty())
    }

    @Test
    fun `file with unknown header is ignored`() {
        registryFile.writeText("# something else\nCONVERTER\tcom.example.A\t/src/A.kt\n")
        assertTrue(TypeConverterRegistry.load(registryFile).isEmpty())
    }

    @Test
    fun `malformed lines are skipped`() {
        TypeConverterRegistry.save(registryFile, listOf(Entry(Kind.CONVERTER, "com.example.A", "/src/A.kt")))
        registryFile.appendText("BROKEN LINE\nNOT_A_KIND\tcom.example.B\t/src/B.kt\n")
        assertEquals(listOf("com.example.A"), TypeConverterRegistry.load(registryFile).map { it.qualifiedName })
    }

    @Test
    fun `survivors drops reprocessed and vanished origins`() {
        val previous =
            listOf(
                Entry(Kind.CONVERTER, "com.example.Kept", "/src/Kept.kt"),
                Entry(Kind.CONVERTER, "com.example.Reprocessed", "/src/Reprocessed.kt"),
                Entry(Kind.IMPORTER, "com.example.Vanished", "/src/Vanished.kt"),
            )
        val survivors =
            TypeConverterRegistry.survivors(
                previous,
                reprocessedFilePaths = setOf("/src/Reprocessed.kt"),
            ) { it != "/src/Vanished.kt" }
        assertEquals(listOf("com.example.Kept"), survivors.map { it.qualifiedName })
    }
}
