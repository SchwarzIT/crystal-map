package com.schwarz.crystalksp.model.source

import com.google.devtools.ksp.symbol.KSAnnotation
import com.schwarz.crystalcore.model.source.ISourceComment
import com.schwarz.crystalksp.util.getArgument

class SourceComment(
    annoation: KSAnnotation,
) : ISourceComment {
    override val comment: Array<String> =
        annoation.getArgument<List<String>>("comment")?.toTypedArray() ?: arrayOf()
}
