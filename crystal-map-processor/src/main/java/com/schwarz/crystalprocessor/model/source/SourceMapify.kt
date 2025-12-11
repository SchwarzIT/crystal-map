package com.schwarz.crystalprocessor.model.source

import com.schwarz.crystalapi.mapify.Mapify
import com.schwarz.crystalcore.model.source.ISourceMapify

class SourceMapify(
    mapifyAnnotation: Mapify,
) : ISourceMapify {
    override val name: String = mapifyAnnotation.name
    override val nullableIndexes: List<Int> = mapifyAnnotation.nullableIndexes.toList()
}
