package com.schwarz.crystalcore.util

import java.io.File

/**
 * Writes [content] to this file unless the file already contains exactly [content].
 * Skipping identical rewrites keeps file timestamps stable for consumers that
 * watch these side outputs (documentation, schema exports).
 */
fun File.writeTextIfChanged(content: String) {
    if (exists() && readText() == content) {
        return
    }
    writeText(content)
}
