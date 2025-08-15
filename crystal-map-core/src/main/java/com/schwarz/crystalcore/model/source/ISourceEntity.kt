package com.schwarz.crystalcore.model.source

import com.schwarz.crystalapi.Entity

interface ISourceEntity {

    val modifierOpen: Boolean
    val type: Entity.Type
    val database: String
}