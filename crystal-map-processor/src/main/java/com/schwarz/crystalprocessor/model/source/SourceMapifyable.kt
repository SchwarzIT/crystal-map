package com.schwarz.crystalprocessor.model.source

import com.schwarz.crystalapi.mapify.Mapifyable
import com.schwarz.crystalcore.model.source.ISourceDeclaringName
import com.schwarz.crystalcore.model.source.ISourceMapifyable
import com.schwarz.crystalprocessor.ProcessingContext
import com.schwarz.crystalprocessor.util.FieldExtractionUtil

class SourceMapifyable(
    private val mapifyableAnnotations: Mapifyable,
) : ISourceMapifyable {
    private val valueTypeMirror = mapifyableAnnotations.let { FieldExtractionUtil.typeMirror(it) }

    override val valueDeclaringName: ISourceDeclaringName
        get() = ProcessingContext.DeclaringName(valueTypeMirror!!)
}
