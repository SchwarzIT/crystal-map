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

    init {
        deprecatedFields.forEach {
            print("field ${it.key}, value ${it.value.inUse}")
        }
    }

    fun addDeprecated(field: String, spec: PropertySpec.Builder) {

        deprecatedFields[field]?.let {
            print("field ${it.field} inUse ${it.inUse}")
            spec.addAnnotation(buildDeprecatedAnnotation(it.inUse, it.replacedBy))
        }
    }

    fun evaluateFieldDeprecationLevel(field: String) : DeprecationLevel? = deprecatedFields[field]?.let { evaluateDeprecationLevel(it.inUse) }

    fun addDeprecated(field: String, spec: FunSpec.Builder) : Boolean {

        return deprecatedFields[field]?.let {
            print("field ${it.field} inUse ${it.inUse}")
            spec.addAnnotation(buildDeprecatedAnnotation(it.inUse, it.replacedBy))
            true
        } ?: false
    }

    private fun evaluateDeprecationLevel(inUse: Boolean) = if (inUse) {
        DeprecationLevel.WARNING
    } else {
        DeprecationLevel.ERROR
    }

    private fun buildDeprecatedAnnotation(inUse: Boolean, replaceWith: String?): AnnotationSpec {
        val builder = AnnotationSpec.builder(kotlin.Deprecated::class.java)
        builder.addMember("message = %S", "will be removed in future release")
        builder.addMember("level = %T.${evaluateDeprecationLevel(inUse)}", DeprecationLevel::class.java)
        replaceWith?.let {
            if (it.isNotBlank()) {
                builder.addMember("replaceWith = %T(%S)", ReplaceWith::class.java, it)
            }
        }
        return builder.build()
    }

    fun addDeprecated(spec: TypeSpec.Builder) {

        replacedByTypeMirror?.let {
            if (it.toString() != Void::class.java.canonicalName) {
                spec.addAnnotation(buildDeprecatedAnnotation(inUse, it.toString()))
            }
        }

    }


}