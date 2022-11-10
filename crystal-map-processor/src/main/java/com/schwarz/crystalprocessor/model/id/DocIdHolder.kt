package com.schwarz.crystalprocessor.model.id

import com.schwarz.crystalprocessor.model.entity.BaseEntityHolder
import com.schwarz.crystalprocessor.model.field.CblFieldHolder
import com.schwarz.crystalprocessor.util.TypeUtil
import com.squareup.kotlinpoet.*
import com.schwarz.crystalapi.DocId
import java.util.regex.Pattern

class DocIdHolder(docId: DocId, val customSegmentSource: MutableList<DocIdSegmentHolder>) {

    private val docIdSegmentCallPattern = Pattern.compile("\\((.+?)\\)")

    data class Segment(
        val segment: String,
        val fields: List<String>,
        val customSegment: DocIdSegmentHolder?
    ) {

        fun fieldsToModelFields(entity: BaseEntityHolder) = fields.map {
            entity.fields[it] ?: entity.fieldConstants[it]
                ?: throw Exception("type [$it] not found in model fields")
        }
    }

    val pattern = docId.value

    init {
        recompile()
    }

    fun recompile() {
        customSegments = customSegmentSource.associateBy { it.name }.toMutableMap()
        segments = Pattern.compile("%(.+?)%").matcher(pattern).let {
            val segments = mutableListOf<Segment>()
            while (it.find()) {
                val plainSegment = it.group(1)
                val segmentMatcher = docIdSegmentCallPattern.matcher(plainSegment)
                if (segmentMatcher.find()) {
                    val fields = segmentMatcher.group(1).split(',').map { it.trim() }
                    segments.add(
                        Segment(
                            plainSegment,
                            fields,
                            this.customSegments[
                                plainSegment.removeSuffix("(${segmentMatcher.group(1)})")
                                    .removePrefix("this.")
                            ]
                        )
                    )
                } else if (plainSegment.contains("()")) {
                    segments.add(
                        Segment(
                            plainSegment,
                            listOf(),
                            this.customSegments[
                                plainSegment.removeSuffix("()")
                                    .removePrefix("this.")
                            ]
                        )
                    )
                } else {
                    segments.add(Segment(plainSegment, listOf(plainSegment), null))
                }
            }
            segments
        }
    }

    lateinit var customSegments: MutableMap<String, DocIdSegmentHolder>

    // contains all segments placed between %
    lateinit var segments: List<Segment>

    private fun List<Segment>.distinctFieldAccessors(model: BaseEntityHolder) =
        this.map { it.fieldsToModelFields(model) }.flatten().map { it.accessorSuffix() }.distinct()

    fun companionFunction(entity: BaseEntityHolder): FunSpec {
        val spec = FunSpec.builder(COMPANION_BUILD_FUNCTION_NAME).addAnnotation(JvmStatic::class)
            .returns(TypeUtil.string())
        var statement = pattern
        val addedFields = mutableListOf<String>()
        for (segment in segments) {
            val entityFields = segment.fieldsToModelFields(entity).associateBy { it.dbField }

            val statementValue = segment.customSegment?.let {
                "\${${it.name}(${
                entityFields.map { it.value.accessorSuffix() }.joinToString(separator = ",")
                })}"
            }
                ?: entityFields.map { it.value.accessorSuffix() }
                    .joinToString(separator = ",") { "\$$it" }

            statement = statement.replace("%${segment.segment}%", statementValue)

            entityFields.values.forEach {
                if (!addedFields.contains(it.dbField)) {
                    addedFields.add(it.dbField)
                    spec.addParameter(
                        it.accessorSuffix(),
                        TypeUtil.parseMetaType(
                            it.typeMirror,
                            it.isIterable,
                            (it as? CblFieldHolder?)?.subEntitySimpleName
                        ).copy(nullable = !it.isConstant)
                    )
                }
            }
        }

        spec.addStatement("return %P", statement)
        return spec.build()
    }

    fun buildExpectedDocId(entity: BaseEntityHolder): FunSpec {
        val spec = FunSpec.builder(BUILD_FUNCTION_NAME).returns(TypeUtil.string())
            .addModifiers(KModifier.OVERRIDE)
        val list: List<String> = segments.distinctFieldAccessors(entity)

        spec.addStatement("return $COMPANION_BUILD_FUNCTION_NAME(${list.joinToString(separator = ",")})")
        return spec.build()
    }

    companion object {
        const val BUILD_FUNCTION_NAME = "buildExpectedDocId"
        const val COMPANION_BUILD_FUNCTION_NAME = "buildDocId"
    }
}
