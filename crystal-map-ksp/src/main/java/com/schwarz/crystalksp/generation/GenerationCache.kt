package com.schwarz.crystalksp.generation

import java.io.File
import java.security.MessageDigest

class GenerationCache(
    private val cacheFile: File,
) {
    private val previousHashes: Map<String, String>
    private val currentHashes: MutableMap<String, String> = mutableMapOf()

    init {
        previousHashes = if (cacheFile.exists()) {
            loadFromFile(cacheFile)
        } else {
            emptyMap()
        }
    }

    fun shouldGenerate(packageName: String, fileName: String, content: String): Boolean {
        val key = "$packageName/$fileName"
        val hash = sha256(content)
        currentHashes[key] = hash
        return previousHashes[key] != hash
    }

    fun save() {
        cacheFile.parentFile?.mkdirs()
        cacheFile.bufferedWriter().use { writer ->
            currentHashes.entries.sortedBy { it.key }.forEach { (key, hash) ->
                writer.write("$key\t$hash")
                writer.newLine()
            }
        }
    }

    companion object {
        private fun sha256(content: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(content.toByteArray(Charsets.UTF_8))
            return hashBytes.joinToString("") { "%02x".format(it) }
        }

        private fun loadFromFile(file: File): Map<String, String> {
            val result = mutableMapOf<String, String>()
            file.bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    val parts = line.split('\t', limit = 2)
                    if (parts.size == 2) {
                        result[parts[0]] = parts[1]
                    }
                }
            }
            return result
        }
    }
}
