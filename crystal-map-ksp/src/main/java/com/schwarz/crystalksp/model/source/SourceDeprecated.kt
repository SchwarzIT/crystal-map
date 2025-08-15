package com.schwarz.crystalksp.model.source

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSType
import com.schwarz.crystalapi.deprecated.DeprecationType
import com.schwarz.crystalcore.model.source.ISourceDeprecated
import com.schwarz.crystalksp.util.getArgument
import com.schwarz.crystalksp.util.getEnumArgument
import com.squareup.kotlinpoet.ksp.toClassName

class SourceDeprecated(val deprecatedAnnotation: KSAnnotation) : ISourceDeprecated {

    private val replacedByTypeMirror = deprecatedAnnotation.getArgument<KSType>("replacedBy")

    private val replaceByCanonicalName = replacedByTypeMirror?.let {
        it.toClassName().canonicalName
    }

    override val replacedBy = replacedByTypeMirror?.let {
        if (replaceByCanonicalName != Void::class.java.canonicalName) {
            replaceByCanonicalName
        } else {
            ""
        }
    } ?: ""
    override val type: DeprecationType = deprecatedAnnotation.getEnumArgument<DeprecationType>("type") ?: DeprecationType.FIELD_DEPRECATION
    override val fields: Array<ISourceDeprecated.ISourceDeprecatedField> = deprecatedAnnotation.getArgument<List<KSAnnotation>>("fields")?.map {
        ISourceDeprecated.ISourceDeprecatedField(it.getArgument("field") ?: "", it.getArgument("replacedBy") ?: "", it.getArgument("inUse") ?: false)
    }?.toTypedArray() ?: arrayOf()
}
