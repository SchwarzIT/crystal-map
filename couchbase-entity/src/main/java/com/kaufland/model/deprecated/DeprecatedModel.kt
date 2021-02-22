package com.kaufland.model.deprecated

import com.kaufland.util.FieldExtractionUtil
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import kaufland.com.coachbasebinderapi.deprecated.Deprecated
import kaufland.com.coachbasebinderapi.deprecated.DeprecatedField
import javax.lang.model.type.TypeMirror

class DeprecatedModel(deprecated: Deprecated) {

    val replacedByTypeMirror: TypeMirror? = FieldExtractionUtil.typeMirror(deprecated)

    val inUse: Boolean = deprecated.inUse

    val deprecatedFields: Map<String, DeprecatedField> = deprecated.fields.map { it.field to it }.toMap()

    fun addDeprecated(field: String, spec: PropertySpec.Builder) {

        deprecatedFields[field]?.let {
            val builder = AnnotationSpec.builder(kotlin.Deprecated::class.java)

            builder.addMember("message = %S", "will be removed in future release")
            builder.addMember("level = %T.${evaluateDeprecationLevel(it.inUse)}", DeprecationLevel::class.java)

            if (it.replacedBy.isNotBlank()) {
                builder.addMember("replaceWith = %T(%S)", ReplaceWith::class.java, it.replacedBy)
            }

            spec.addAnnotation(builder.build())
        }
    }

    private fun evaluateDeprecationLevel(inUse: Boolean) = if (inUse) {
        DeprecationLevel.WARNING
    } else {
        DeprecationLevel.ERROR
    }

    fun addDeprecated(spec: TypeSpec.Builder) {

        replacedByTypeMirror?.let {
            if (it.toString() != Void::class.java.canonicalName) {
                val builder = AnnotationSpec.builder(kotlin.Deprecated::class.java)
                builder.addMember("message = %S", "will be removed in future release")
                builder.addMember("level = %T.${evaluateDeprecationLevel(inUse)}", DeprecationLevel::class.java)
                builder.addMember("replaceWith = %T(%S)", ReplaceWith::class.java, it.toString())
                spec.addAnnotation(builder.build())
            }
        }

    }


}