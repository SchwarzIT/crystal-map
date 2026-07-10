package com.schwarz.crystalcore.util

import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * Writes [content] to this file unless the file already contains exactly [content].
 * Skipping identical rewrites keeps file timestamps stable for consumers that
 * watch these side outputs (documentation, schema exports).
 */
fun File.writeTextIfChanged(content: String) {
    writeTextIfChanged(content, readTextOrNull())
}

/**
 * Variant of [writeTextIfChanged] for callers that already read the file:
 * pass the previously read content as [previousContent] to avoid a second read.
 */
fun File.writeTextIfChanged(
    content: String,
    previousContent: String?,
) {
    if (previousContent == content) {
        return
    }
    writeTextAtomically(content)
}

fun File.readTextOrNull(): String? =
    if (exists()) {
        runCatching { readText() }.getOrNull()
    } else {
        null
    }

/**
 * Decodes [text] as JSON, returning null for null input or malformed content.
 * Shared recovery behavior for all generators that persist a model of their
 * previous run: a missing or corrupt model must never fail the build.
 */
inline fun <reified T> decodeJsonOrNull(text: String?): T? =
    text?.let {
        runCatching { Json.decodeFromString<T>(it) }.getOrNull()
    }

/**
 * Writes via a temp file plus (atomic, where supported) rename so concurrent
 * readers of the shared side-output files never observe a torn write.
 */
private fun File.writeTextAtomically(content: String) {
    val directory = parentFile ?: File(".")
    val temp = Files.createTempFile(directory.toPath(), name, ".tmp")
    try {
        Files.writeString(temp, content)
        try {
            Files.move(temp, toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        } catch (e: AtomicMoveNotSupportedException) {
            Files.move(temp, toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
    } finally {
        Files.deleteIfExists(temp)
    }
}
