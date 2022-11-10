package com.schwarz.crystalprocessor.generation.model

import com.squareup.kotlinpoet.CodeBlock

object KDocGeneration {
    fun generate(comments: Array<String>): CodeBlock {
        return CodeBlock.of("%L", comments.joinToString(separator = "\n"))
    }
}
