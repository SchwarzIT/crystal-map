package com.schwarz.crystalprocessor.model.source

import com.schwarz.crystalapi.BasedOn
import com.schwarz.crystalcore.model.source.ISourceBasedOn
import com.schwarz.crystalprocessor.util.FieldExtractionUtil

class SourceBasedOn(override val basedOnAnnotation: BasedOn) : ISourceBasedOn {

    private val basedOnValues = basedOnAnnotation.let { FieldExtractionUtil.typeMirror(it) }

    override val basedOnFullQualifiedNames = basedOnValues.map { it.toString() }
}
