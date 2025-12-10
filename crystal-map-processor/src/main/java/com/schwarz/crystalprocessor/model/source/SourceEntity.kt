package com.schwarz.crystalprocessor.model.source

import com.schwarz.crystalapi.Entity
import com.schwarz.crystalcore.model.source.ISourceEntity

class SourceEntity(
    private val entityAnnotation: Entity,
) : ISourceEntity {
    override val modifierOpen: Boolean
        get() = entityAnnotation.modifierOpen
    override val type: Entity.Type
        get() = entityAnnotation.type
    override val database: String
        get() = entityAnnotation.database
}
