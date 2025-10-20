package com.schwarz.crystalksp.model.source

import com.google.devtools.ksp.symbol.KSAnnotation
import com.schwarz.crystalcore.model.source.ISourceQuery
import com.schwarz.crystalksp.util.getArgument

class SourceQuery(private val queryAnnotations: KSAnnotation) : ISourceQuery {
    override val fields: Array<String> = queryAnnotations.getArgument<List<String>>("fields")?.toTypedArray() ?: arrayOf()
}
