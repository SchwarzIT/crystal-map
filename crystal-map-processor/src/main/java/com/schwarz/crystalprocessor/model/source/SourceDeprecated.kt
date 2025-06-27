package com.schwarz.crystalprocessor.model.source

import com.schwarz.crystalapi.deprecated.Deprecated
import com.schwarz.crystalcore.model.source.ISourceDeprecated
import com.schwarz.crystalprocessor.util.FieldExtractionUtil
import javax.lang.model.type.TypeMirror

class SourceDeprecated(override val deprecatedAnnotation: Deprecated) : ISourceDeprecated {

    private val replacedByTypeMirror: TypeMirror? =
        FieldExtractionUtil.typeMirror(deprecatedAnnotation)

    override val replacedBy = replacedByTypeMirror?.let {
        if (it.toString() != Void::class.java.canonicalName) {
            it.toString()
        } else {
            ""
        }
    } ?: ""
}
