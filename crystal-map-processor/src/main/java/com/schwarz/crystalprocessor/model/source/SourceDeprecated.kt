package com.schwarz.crystalprocessor.model.source

import com.schwarz.crystalapi.deprecated.Deprecated
import com.schwarz.crystalapi.deprecated.DeprecatedField
import com.schwarz.crystalapi.deprecated.DeprecationType
import com.schwarz.crystalcore.model.source.ISourceDeprecated
import com.schwarz.crystalprocessor.util.FieldExtractionUtil
import javax.lang.model.type.TypeMirror

class SourceDeprecated(private val deprecatedAnnotation: Deprecated) : ISourceDeprecated {

    private val replacedByTypeMirror: TypeMirror? =
        FieldExtractionUtil.typeMirror(deprecatedAnnotation)

    override val replacedBy = replacedByTypeMirror?.let {
        if (it.toString() != Void::class.java.canonicalName) {
            it.toString()
        } else {
            ""
        }
    } ?: ""
    override val type: DeprecationType= deprecatedAnnotation.type
    override val fields: Array<ISourceDeprecated.ISourceDeprecatedField> = deprecatedAnnotation.fields.map { ISourceDeprecated.ISourceDeprecatedField(it.field, it.replacedBy, it.inUse) }.toTypedArray()
}
