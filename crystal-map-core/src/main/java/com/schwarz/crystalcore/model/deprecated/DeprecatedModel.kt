package com.schwarz.crystalcore.model.deprecated

import com.schwarz.crystalapi.deprecated.DeprecationType
import com.schwarz.crystalcore.model.source.ISourceDeprecated
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

class DeprecatedModel(private val sourceDeprecated: ISourceDeprecated) {
    val deprecationType: DeprecationType = sourceDeprecated.type

    val deprecatedFields: Map<String, ISourceDeprecated.ISourceDeprecatedField> =
        sourceDeprecated.fields.map { it.field to it }.toMap()

    val replacedBy: String = sourceDeprecated.replacedBy

    init {
        deprecatedFields.forEach {
            print("field ${it.key}, value ${it.value.inUse}")
        }
    }

    fun addDeprecated(
        field: String,
        spec: PropertySpec.Builder
    ) {
        if (deprecationType != DeprecationType.FIELD_DEPRECATION) {
            val inUse = deprecationType == DeprecationType.ENTITY_DEPRECATION
            spec.addAnnotation(buildDeprecatedAnnotation(inUse, ""))
        } else {
            deprecatedFields[field]?.let {
                print("field ${it.field} inUse ${it.inUse}")
                spec.addAnnotation(buildDeprecatedAnnotation(it.inUse, it.replacedBy))
            }
        }
    }

    fun evaluateFieldDeprecationLevel(field: String): DeprecationLevel? {
        if (deprecationType != DeprecationType.FIELD_DEPRECATION) {
            val inUse = deprecationType == DeprecationType.ENTITY_DEPRECATION
            return evaluateDeprecationLevel(inUse)
        }

        return deprecatedFields[field]?.let { evaluateDeprecationLevel(it.inUse) }
    }

    fun evaluateSummedFieldDeprecationLevel(vararg fields: String): DeprecationLevel? {
        return fields.mapNotNull { evaluateFieldDeprecationLevel(it) }.maxByOrNull { it.ordinal }
    }

    fun addDeprecatedFunctions(
        fields: Array<String>,
        spec: FunSpec.Builder
    ): Boolean {
        return if (deprecationType != DeprecationType.FIELD_DEPRECATION) {
            val inUse = deprecationType == DeprecationType.ENTITY_DEPRECATION
            spec.addAnnotation(
                buildDeprecatedAnnotation(
                    inUse,
                    "check corresponding field replacement"
                )
            )
            inUse.not()
        } else {
            evaluateSummedFieldDeprecationLevel(*fields)?.let {
                val inUse = it == DeprecationLevel.WARNING
                spec.addAnnotation(
                    buildDeprecatedAnnotation(
                        inUse,
                        "check corresponding field replacement"
                    )
                )
                inUse.not()
            } ?: false
        }
    }

    fun addDeprecatedBuilderSetter(
        field: String,
        spec: FunSpec.Builder
    ): Boolean {
        return if (deprecationType != DeprecationType.FIELD_DEPRECATION) {
            val inUse = deprecationType == DeprecationType.ENTITY_DEPRECATION
            spec.addAnnotation(buildDeprecatedAnnotation(inUse, ""))
            inUse.not()
        } else {
            deprecatedFields[field]?.let {
                print("field ${it.field} inUse ${it.inUse}")
                spec.addAnnotation(buildDeprecatedAnnotation(it.inUse, it.replacedBy))
                it.inUse.not()
            } ?: false
        }
    }

    private fun evaluateDeprecationLevel(inUse: Boolean) =
        if (inUse) {
            DeprecationLevel.WARNING
        } else {
            DeprecationLevel.ERROR
        }

    private fun buildDeprecatedAnnotation(
        inUse: Boolean,
        replaceWith: String?
    ): AnnotationSpec {
        val builder = AnnotationSpec.builder(kotlin.Deprecated::class.java)
        builder.addMember("message = %S", "will be removed in future release")
        builder.addMember(
            "level = %T.${evaluateDeprecationLevel(inUse)}",
            DeprecationLevel::class.java
        )
        replaceWith?.let {
            if (it.isNotBlank()) {
                builder.addMember("replaceWith = %T(%S)", ReplaceWith::class.java, it)
            }
        }
        return builder.build()
    }

    fun addDeprecated(spec: TypeSpec.Builder) {
        if (deprecationType == DeprecationType.ENTITY_DEPRECATION || deprecationType == DeprecationType.ENTITY_DEPRECATION_NOT_IN_USE) {
            spec.addAnnotation(buildDeprecatedAnnotation(true, sourceDeprecated.replacedBy))
        }
    }
}
