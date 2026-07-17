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
}
