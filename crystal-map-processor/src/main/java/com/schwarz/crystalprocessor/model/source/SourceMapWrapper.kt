package com.schwarz.crystalprocessor.model.source

import com.schwarz.crystalapi.MapWrapper
import com.schwarz.crystalcore.model.source.ISourceMapWrapper

class SourceMapWrapper(
    private val entityAnnotation: MapWrapper,
) : ISourceMapWrapper {
    override val modifierOpen: Boolean
        get() = entityAnnotation.modifierOpen
}
