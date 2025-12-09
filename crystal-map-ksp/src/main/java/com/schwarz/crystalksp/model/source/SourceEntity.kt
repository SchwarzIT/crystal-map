package com.schwarz.crystalksp.model.source

import com.google.devtools.ksp.symbol.KSAnnotation
import com.schwarz.crystalapi.Entity
import com.schwarz.crystalcore.model.source.ISourceEntity
import com.schwarz.crystalksp.util.getArgument
import com.schwarz.crystalksp.util.getEnumArgument

class SourceEntity(annotation: KSAnnotation) : ISourceEntity {
    override val modifierOpen: Boolean = annotation.getArgument<Boolean>("modifierOpen") ?: false
    override val type: Entity.Type = annotation.getEnumArgument<Entity.Type>("type") ?: Entity.Type.READONLY
    override val database: String = annotation.getArgument<String>("database") ?: ""
}
