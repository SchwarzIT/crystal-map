package com.schwarz.crystalcore.util

import java.io.File

/**
 * Incremental runs only see the reprocessed subset of the entities, so a
 * previous entry survives unless there is positive evidence that it is gone:
 * it was superseded by the current run, all of its source files were deleted,
 * or one of its source files was reprocessed in this run without producing the
 * entry again (annotation removed).
 *
 * Entries without recorded sources (kapt origin, entries reconstructed from a
 * pre-sidecar output) can never produce that evidence and survive until they
 * are superseded — [onUnpurgeableEntries] receives them so the caller can warn
 * that deleting such an entity requires deleting the output file.
 */
fun <V> mergeSideOutputEntries(
    previousEntries: Map<String, V>,
    previousSources: Map<String, List<String>>,
    currentEntries: Map<String, V>,
    reprocessedFilePaths: Set<String>,
    onUnpurgeableEntries: (Set<String>) -> Unit = {},
): Map<String, V> {
    val merged =
        (
            previousEntries.filterKeys { name ->
                keepPreviousEntry(name, previousSources[name].orEmpty(), currentEntries.keys, reprocessedFilePaths)
            } + currentEntries
        ).toSortedMap()
    val unpurgeable =
        merged.keys.filterTo(mutableSetOf()) {
            it !in currentEntries && previousSources[it].isNullOrEmpty()
        }
    if (unpurgeable.isNotEmpty()) {
        onUnpurgeableEntries(unpurgeable)
    }
    return merged
}

internal fun keepPreviousEntry(
    name: String,
    sources: List<String>,
    currentNames: Set<String>,
    reprocessedFilePaths: Set<String>,
): Boolean {
    if (name in currentNames) {
        return false // superseded by the current run
    }
    if (sources.isEmpty()) {
        return true // unknown origin (e.g. kapt): keep conservatively
    }
    if (sources.any { it in reprocessedFilePaths }) {
        return false // source was reprocessed but no longer produces this entry
    }
    if (sources.none { File(it).exists() }) {
        return false // all source files deleted
    }
    return true
}
