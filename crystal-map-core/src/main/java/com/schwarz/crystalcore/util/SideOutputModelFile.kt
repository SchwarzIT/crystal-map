package com.schwarz.crystalcore.util

import java.io.File

/**
 * The persisted sidecar of a side-output generator (documentation,
 * entity-relationship graph, schema): a `<output>.model` file holding the model
 * of the last run, so partial (incremental) runs can merge and purge instead of
 * shrinking the output to the reprocessed subset of entities.
 *
 * The sidecar is deliberately not named *.json: the versioning plugin parses
 * every *.json file in its schema directories.
 */
class SideOutputModelFile(
    directory: File,
    outputFileName: String,
) {
    private val file = File(directory, "$outputFileName.model")

    fun readText(): String? = file.readTextOrNull()

    fun persist(
        encodedModel: String,
        previousText: String?,
    ) {
        file.writeTextIfChanged(encodedModel, previousText ?: file.readTextOrNull())
    }

    /**
     * The sidecar is the authoritative previous-run model; an output written
     * before the sidecar existed (or whose sidecar is corrupt) is recovered by
     * [reconstruct], which receives the unusable sidecar text. Recovered entries
     * carry no source paths and are kept conservatively by the merge until a
     * later run reprocesses them.
     */
    fun <M : Any> loadPrevious(
        mergeWithPrevious: Boolean,
        decode: (String?) -> M?,
        reconstruct: (String?) -> M?,
    ): PreviousModel<M> {
        if (!mergeWithPrevious) {
            return PreviousModel(model = null, text = null)
        }
        val text = readText()
        return PreviousModel(model = decode(text) ?: reconstruct(text), text = text)
    }

    data class PreviousModel<M>(
        val model: M?,
        val text: String?,
    )
}

fun unpurgeableEntriesWarning(
    outputFileName: String,
    names: Set<String>,
): String =
    "Side output $outputFileName contains entries without recorded source files " +
        "(${names.sorted().joinToString()}); deleting one of these entities only takes " +
        "effect after deleting the output file."
