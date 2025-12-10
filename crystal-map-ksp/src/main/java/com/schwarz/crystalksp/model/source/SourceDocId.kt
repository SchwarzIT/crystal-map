package com.schwarz.crystalksp.model.source

import com.google.devtools.ksp.symbol.KSAnnotation
import com.schwarz.crystalcore.model.source.ISourceDocId
import com.schwarz.crystalksp.util.getArgument

class SourceDocId(annotation: KSAnnotation) : ISourceDocId {
    override val value: String = annotation.getArgument<String>("value") ?: ""
}
