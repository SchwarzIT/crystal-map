package com.schwarz.crystalcore.util

import java.io.File

/**
 * Merges the entries of the current processing run over the persisted entries
 * of the previous run. Incremental runs only see the reprocessed subset of the
 * entities, so a previous entry survives unless there is positive evidence
 * that it is gone: it was superseded by the current run, all of its source
 * files were deleted, or one of its source files was reprocessed in this run
 * without producing the entry again (annotation removed).
 */
fun <V> mergeSideOutputEntries(
    previousEntries: Map<String, V>,
    previousSources: Map<String, List<String>>,
    currentEntries: Map<String, V>,
    reprocessedFilePaths: Set<String>,
): Map<String, V> =
    (
        previousEntries.filterKeys { name ->
            keepPreviousEntry(name, previousSources[name].orEmpty(), currentEntries.keys, reprocessedFilePaths)
        } + currentEntries
    ).toSortedMap()

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
    if (sources.none { File(it).exists() }) {
        return false // all source files deleted
    }
    if (sources.any { it in reprocessedFilePaths }) {
        return false // source was reprocessed but no longer produces this entry
    }
    return true
}
