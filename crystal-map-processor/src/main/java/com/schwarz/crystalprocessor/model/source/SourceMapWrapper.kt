package com.schwarz.crystalprocessor.model.source

import com.schwarz.crystalapi.Entity
import com.schwarz.crystalapi.MapWrapper
import com.schwarz.crystalapi.query.Query
import com.schwarz.crystalcore.model.source.ISourceEntity
import com.schwarz.crystalcore.model.source.ISourceMapWrapper
import com.schwarz.crystalcore.model.source.ISourceQuery

class SourceMapWrapper(private val entityAnnotation: MapWrapper) : ISourceMapWrapper {
    override val modifierOpen: Boolean
        get() = entityAnnotation.modifierOpen
}
