package com.schwarz.crystalksp

import com.schwarz.crystalcore.util.readTextOrNull
import com.schwarz.crystalcore.util.writeTextIfChanged
import java.io.File

/**
 * Persisted registry of the module's type-converter world, merged across
 * incremental KSP runs. Converters and importers are global state that dirty
 * entities need but cannot reach: getSymbolsWithAnnotation and getAllFiles
 * only cover the dirty files of an incremental run, and nothing in an entity
 * source references a converter class. The registry keeps their qualified
 * names (with the origin source file for purging) outside KSP's managed
 * outputs — the same merge/purge pattern as the doc/schema side outputs — so
 * every run restores the full set by resolving the names against the current
 * Resolver instead of forcing the converter sources dirty, which an
 * aggregating output would do and which cascades into a full rebuild.
 *
 * A stale or missing file never breaks correctness permanently: entries that
 * no longer resolve are dropped, and a failed run makes KSP delete all
 * outputs, so the next run reprocesses everything and rewrites the registry
 * from scratch.
 */
object TypeConverterRegistry {
    const val FILE_NAME = "type-converter-registry.tsv"

    // Bumped when the line format changes; a file with a different header is
    // ignored entirely (worst case: one full reprocessing, see class KDoc).
    private const val HEADER = "# crystal-map type-converter registry v1"

    enum class Kind { CONVERTER, IMPORTER }

    data class Entry(
        val kind: Kind,
        val qualifiedName: String,
        val originPath: String,
    )

    fun load(file: File): List<Entry> {
        val lines = file.readTextOrNull()?.lines() ?: return emptyList()
        if (lines.firstOrNull() != HEADER) {
            return emptyList()
        }
        return lines.drop(1).mapNotNull { line ->
            val parts = line.split('\t')
            if (parts.size != 3 || parts.any(String::isEmpty)) {
                null
            } else {
                val kind = Kind.entries.find { it.name == parts[0] } ?: return@mapNotNull null
                Entry(kind, parts[1], parts[2])
            }
        }
    }

    /**
     * Previous-run entries that are still trustworthy: entries whose origin
     * file was reprocessed are dropped (they get re-collected in this run if
     * the class still carries its annotation), as are entries whose origin
     * file disappeared.
     */
    fun survivors(
        previous: List<Entry>,
        reprocessedFilePaths: Set<String>,
        originExists: (String) -> Boolean,
    ): List<Entry> =
        previous.filter { entry ->
            entry.originPath !in reprocessedFilePaths && originExists(entry.originPath)
        }

    fun save(
        file: File,
        entries: List<Entry>,
    ) {
        val content =
            buildString {
                appendLine(HEADER)
                entries
                    .sortedWith(compareBy({ it.kind }, { it.qualifiedName }))
                    .forEach { appendLine("${it.kind.name}\t${it.qualifiedName}\t${it.originPath}") }
            }
        file.parentFile?.mkdirs()
        file.writeTextIfChanged(content)
    }
}
