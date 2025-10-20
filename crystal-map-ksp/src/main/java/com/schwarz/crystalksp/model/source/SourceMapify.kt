package com.schwarz.crystalksp.model.source

import com.google.devtools.ksp.symbol.KSAnnotation
import com.schwarz.crystalcore.model.source.ISourceMapify
import com.schwarz.crystalksp.util.getArgument

class SourceMapify(val mapifyAnnotations: KSAnnotation) : ISourceMapify {
    override val name: String = mapifyAnnotations.getArgument<String>("name")!!
    override val nullableIndexes: List<Int> = mapifyAnnotations.getArgument<List<Int>>("nullableIndexes")?.toList() ?: listOf()
}
