package com.schwarz.crystalksp.model.source

import com.google.devtools.ksp.symbol.KSAnnotation
import com.schwarz.crystalcore.model.source.ISourceMapWrapper
import com.schwarz.crystalksp.util.getArgument

class SourceMapWrapper(annotation: KSAnnotation) : ISourceMapWrapper {
    override val modifierOpen: Boolean = annotation.getArgument<Boolean>("modifierOpen") ?: false
}
