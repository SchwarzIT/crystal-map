package com.schwarz.crystalprocessor.model

import com.schwarz.crystalprocessor.model.accessor.CblGenerateAccessorHolder
import com.schwarz.crystalprocessor.model.deprecated.DeprecatedModel
import com.schwarz.crystalprocessor.model.entity.BaseEntityHolder
import com.schwarz.crystalprocessor.model.entity.BaseModelHolder
import com.schwarz.crystalprocessor.model.entity.EntityHolder
import com.schwarz.crystalprocessor.model.entity.ReducedModelHolder
import com.schwarz.crystalprocessor.model.entity.WrapperEntityHolder
import com.schwarz.crystalprocessor.model.entity.SchemaClassHolder
import com.schwarz.crystalprocessor.model.field.CblConstantHolder
import com.schwarz.crystalprocessor.model.field.CblFieldHolder
import com.schwarz.crystalprocessor.model.id.DocIdHolder
import com.schwarz.crystalprocessor.model.id.DocIdSegmentHolder
import com.schwarz.crystalprocessor.model.query.CblQueryHolder
import com.schwarz.crystalprocessor.model.source.ISourceModel
import com.schwarz.crystalprocessor.util.FieldExtractionUtil

object EntityFactory {
    private const val WRAPPER_SUB_ENTITY_POST_FIX = "Wrapper"
    private const val SCHEMA_SUB_ENTITY_POST_FIX = "Schema"

    fun createEntityHolder(
        sourceModel: ISourceModel,
        allWrapperPaths: List<String>,
        allBaseModels: Map<String, BaseModelHolder>
    ): EntityHolder {
        val annotation = sourceModel.entityAnnotation!!
        return create(
            sourceModel,
            EntityHolder(
                annotation.database,
                annotation.modifierOpen,
                annotation.type,
                sourceModel,
            ),
            allWrapperPaths,
            allBaseModels,
            WRAPPER_SUB_ENTITY_POST_FIX,
        ) as EntityHolder
    }

    fun createBaseModelHolder(
        sourceModel: ISourceModel,
        allWrapperPaths: List<String>
    ): BaseModelHolder {
        return create(
            sourceModel,
            BaseModelHolder(sourceModel),
            allWrapperPaths,
            emptyMap(),
            WRAPPER_SUB_ENTITY_POST_FIX,
        ) as BaseModelHolder
    }

    fun createChildEntityHolder(
        sourceModel: ISourceModel,
        allWrapperPaths: List<String>,
        allBaseModels: Map<String, BaseModelHolder>
    ): WrapperEntityHolder {
        val annotation = sourceModel.mapWrapperAnnotation!!
        return create(
            sourceModel,
            WrapperEntityHolder(annotation.modifierOpen, sourceModel),
            allWrapperPaths,
            allBaseModels,
            WRAPPER_SUB_ENTITY_POST_FIX,
        ) as WrapperEntityHolder
    }

    fun createSchemaEntityHolder(
        sourceModel: ISourceModel,
        allSchemaClassPaths: List<String>,
        allBaseModels: Map<String, BaseModelHolder>
    ): SchemaClassHolder {
        return create(
            sourceModel,
            SchemaClassHolder(sourceModel),
            allSchemaClassPaths,
            allBaseModels,
            SCHEMA_SUB_ENTITY_POST_FIX,
        ) as SchemaClassHolder
    }

    private fun create(
        sourceModel: ISourceModel,
        content: BaseEntityHolder,
        classPaths: List<String>,
        allBaseModels: Map<String, BaseModelHolder>,
        subEntityNamePostFix: String,
    ): BaseEntityHolder {
        content.reducesModels = createReduceModels(sourceModel, content)
        content.abstractParts = sourceModel.abstractParts
        content.comment = sourceModel.commentAnnotation?.comment ?: arrayOf()
        content.deprecated = sourceModel.deprecatedAnnotation?.let { DeprecatedModel(it) }

        addBasedOn(sourceModel, allBaseModels, content)

        parseQueries(sourceModel, content)
        parseFields(sourceModel, content, classPaths, subEntityNamePostFix)

        val docId = sourceModel.docIdAnnotation
        val docIdSegments: MutableList<DocIdSegmentHolder> = mutableListOf()

        sourceModel.relevantStaticFunctions.forEach {
            if (it.docIdSegment != null) {
                docIdSegments.add(DocIdSegmentHolder(it))
            }
            if (it.generateAccessor != null) {
                content.generateAccessors.add(
                    CblGenerateAccessorHolder(
                        content.sourceClazzTypeName,
                        it,
                        null
                    )
                )
            }
        }
        sourceModel.relevantStaticFields.forEach {
            if (it.generateAccessor != null) {
                content.generateAccessors.add(
                    CblGenerateAccessorHolder(
                        content.sourceClazzTypeName,
                        null,
                        it
                    )
                )
            }
        }
        content.docId = docId?.let { DocIdHolder(it, docIdSegments) } ?: content.docId?.apply {
            customSegmentSource.addAll(docIdSegments)
            recompile()
        }

        return content
    }

    private fun createReduceModels(
        sourceModel: ISourceModel,
        content: BaseEntityHolder
    ): List<ReducedModelHolder> {
        sourceModel.reduceAnnotations.let { reduce ->
            return reduce.map {
                ReducedModelHolder(
                    it.namePrefix,
                    it.include.asList(),
                    it.includeQueries,
                    it.includeAccessors,
                    it.includeDocId,
                    it.includeBasedOn,
                    content
                )
            }
        }
    }

    fun addBasedOn(
        sourceModel: ISourceModel,
        allBaseModels: Map<String, BaseModelHolder>,
        content: BaseEntityHolder
    ) {
        val basedOnValue = sourceModel.basedOnAnnotation?.let { FieldExtractionUtil.typeMirror(it) }

        basedOnValue?.forEach { type ->
            allBaseModels[type.toString()]?.let {
                it.docId?.let {
                    if (content.docId == null) {
                        content.docId = it
                    } else {
                        content.docId?.customSegmentSource?.addAll(it.customSegmentSource)
                        content.docId?.recompile()
                    }
                }
                content.basedOn.add(it)
                it.fieldConstants.forEach {
                    content.fieldConstants.putIfAbsent(it.key, it.value)
                }
                it.fields.forEach {
                    content.fields.putIfAbsent(it.key, it.value)
                }
                content.generateAccessors.addAll(it.generateAccessors)
                content.queries.addAll(it.queries)
                addBasedOn(it.sourceElement, allBaseModels, content)
            }
        }
    }

    private fun parseFields(
        sourceModel: ISourceModel,
        content: BaseEntityHolder,
        classPaths: List<String>,
        subEntityNamePostFix: String
    ) {
        for (cblField in sourceModel.fieldAnnotations) {
            if (cblField.readonly) {
                content.fieldConstants[cblField.name] = CblConstantHolder(cblField)
            } else {
                val cblFieldHolder = CblFieldHolder(cblField, classPaths, subEntityNamePostFix)
                content.fields[cblField.name] = cblFieldHolder
            }
        }
    }

    private fun parseQueries(sourceModel: ISourceModel, content: BaseEntityHolder) {
        val queries = sourceModel.queryAnnotations

        for (cblQuery in queries) {
            content.queries.add(CblQueryHolder(cblQuery))
        }
    }
}
