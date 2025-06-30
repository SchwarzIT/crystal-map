package com.schwarz.crystalcore.model.id

import com.schwarz.crystalcore.model.entity.BaseEntityHolder
import com.schwarz.crystalcore.model.field.CblFieldHolder
import com.squareup.kotlinpoet.*
import com.schwarz.crystalapi.DocId
import com.schwarz.crystalcore.model.typeconverter.TypeConverterHolderForEntityGeneration
import com.schwarz.crystalcore.util.TypeUtil
import java.util.regex.Pattern

class DocIdHolder(docId: DocId, val customSegmentSource: MutableList<DocIdSegmentHolder>) {

    private val docIdSegmentCallPattern = Pattern.compile("\\((.+?)\\)")

    data class Segment(
        val segment: String,
        val fields: List<String>,
        val customSegment: DocIdSegmentHolder?
    ) {

        fun <T>fieldsToModelFields(entity: BaseEntityHolder<T>) = fields.map {
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

    fun <T>distinctFieldAccessors(model: BaseEntityHolder<T>): List<String> {
        return segments.map { it.fieldsToModelFields(model) }.flatten().map { it.accessorSuffix() }.distinct()
    }

    fun <T>companionFunction(entity: BaseEntityHolder<T>, typeConvertersByConvertedClass: Map<TypeName, TypeConverterHolderForEntityGeneration>): FunSpec {
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
                ?: entityFields.map { (_, fieldHolder) ->
                    val accessorSuffix = fieldHolder.accessorSuffix()
                    if (fieldHolder.isNonConvertibleClass) {
                        accessorSuffix
                    } else {
                        val typeConverter = typeConvertersByConvertedClass[fieldHolder.fieldType]!!.instanceClassTypeName.simpleName
                        "{$typeConverter.write($accessorSuffix)}"
                    }
                }
                    .joinToString(separator = ",") { "\$$it" }

            statement = statement.replace("%${segment.segment}%", statementValue)

            entityFields.values.forEach {
                if (!addedFields.contains(it.dbField)) {
                    addedFields.add(it.dbField)
                    spec.addParameter(
                        it.accessorSuffix(),
                        it.mField.parseMetaType(
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

    fun <T>buildExpectedDocId(entity: BaseEntityHolder<T>): FunSpec {
        val spec = FunSpec.builder(BUILD_FUNCTION_NAME).returns(TypeUtil.string())
            .addModifiers(KModifier.OVERRIDE)
        val list: List<String> = distinctFieldAccessors(entity)

        if (entity.deprecated?.addDeprecatedFunctions(list.toTypedArray(), spec) == true) {
            spec.addStatement("throw %T()", UnsupportedOperationException::class)
        } else {
            spec.addStatement("return $COMPANION_BUILD_FUNCTION_NAME(${list.joinToString(separator = ",")})")
        }
        return spec.build()
    }

    companion object {
        const val BUILD_FUNCTION_NAME = "buildExpectedDocId"
        const val COMPANION_BUILD_FUNCTION_NAME = "buildDocId"
    }
}
