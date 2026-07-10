package com.schwarz.crystalcore.util

import java.util.Base64

/**
 * Embeds a machine-readable model into a generated text output (as a Base64
 * payload inside a format-specific comment) and extracts it back. This lets
 * aggregate outputs (documentation, relationship graph) survive incremental
 * processing runs, where only a subset of the entities is (re)processed:
 * the previous model is loaded from the existing file and merged with the
 * segments of the current run.
 */
object EmbeddedModel {
    fun embed(
        prefix: String,
        suffix: String,
        json: String,
    ): String = prefix + Base64.getEncoder().encodeToString(json.toByteArray(Charsets.UTF_8)) + suffix

    fun extract(
        fileContent: String,
        prefix: String,
        suffix: String,
    ): String? =
        fileContent
            .lineSequence()
            .map { it.trim() }
            .firstOrNull { it.startsWith(prefix) && it.endsWith(suffix) }
            ?.removePrefix(prefix)
            ?.removeSuffix(suffix)
            ?.let { payload ->
                runCatching {
                    String(Base64.getDecoder().decode(payload), Charsets.UTF_8)
                }.getOrNull()
            }
}
