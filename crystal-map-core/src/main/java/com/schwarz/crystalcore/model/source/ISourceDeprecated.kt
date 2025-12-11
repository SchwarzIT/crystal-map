package com.schwarz.crystalcore.model.source

import com.schwarz.crystalapi.deprecated.DeprecationType

interface ISourceDeprecated {
    val replacedBy: String

    val type: DeprecationType

    val fields: Array<ISourceDeprecatedField>

    data class ISourceDeprecatedField(
        val field: String,
        val replacedBy: String,
        val inUse: Boolean,
    )
}
