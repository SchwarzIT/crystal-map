package com.schwarz.crystalksp.model.source

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.schwarz.crystalapi.mapify.Mapifyable
import com.schwarz.crystalcore.model.source.ISourceDeclaringName
import com.schwarz.crystalcore.model.source.ISourceMapify
import com.schwarz.crystalcore.model.source.ISourceMapifyable
import com.schwarz.crystalksp.ProcessingContext
import com.schwarz.crystalksp.util.getArgument

class SourceMapify(val mapifyAnnotations: KSAnnotation) : ISourceMapify {
    override val name: String = mapifyAnnotations.getArgument<String>("name")!!
    override val nullableIndexes: List<Int> = mapifyAnnotations.getArgument<List<Int>>("nullableIndexes")?.toList()?: listOf()

}
