package com.schwarz.crystalcore.model.source

import com.schwarz.crystalapi.deprecated.Deprecated

interface ISourceDeprecated {

    val replacedBy: String

    val deprecatedAnnotation: Deprecated
}
