package com.kaufland.model

import com.kaufland.model.accessor.CblGenerateAccessorHolder
import com.kaufland.model.deprecated.DeprecatedModel
import com.kaufland.model.entity.BaseEntityHolder
import com.kaufland.model.entity.BaseModelHolder
import com.kaufland.model.entity.EntityHolder
import com.kaufland.model.entity.ReducedModelHolder
import com.kaufland.model.entity.WrapperEntityHolder
import com.kaufland.model.field.CblConstantHolder
import com.kaufland.model.field.CblFieldHolder
import com.kaufland.model.id.DocIdHolder
import com.kaufland.model.id.DocIdSegmentHolder
import com.kaufland.model.query.CblQueryHolder
import com.kaufland.model.source.ISourceModel
import com.kaufland.util.FieldExtractionUtil

object EntityFactory {

    fun createEntityHolder(
        sourceModel: ISourceModel,
        allWrappers: List<String>,
        allBaseModels: Map<String, BaseModelHolder>
    ): EntityHolder {
        val annotation = sourceModel.entityAnnotation!!
        return create(
            sourceModel,
            EntityHolder(
                annotation.database, annotation.modifierOpen, annotation.type, sourceModel
            ),
            allWrappers,
            allBaseModels
        ) as EntityHolder
    }

    fun createBaseModelHolder(
        sourceModel: ISourceModel,
        allWrappers: List<String>
    ): BaseModelHolder {
        return create(
            sourceModel, BaseModelHolder(sourceModel), allWrappers, emptyMap()
        ) as BaseModelHolder
    }

    fun createChildEntityHolder(
        sourceModel: ISourceModel,
        allWrappers: List<String>,
        allBaseModels: Map<String, BaseModelHolder>
    ): WrapperEntityHolder {
        val annotation = sourceModel.mapWrapperAnnotation!!
        return create(
            sourceModel,
            WrapperEntityHolder(annotation.modifierOpen, sourceModel),
            allWrappers,
            allBaseModels
        ) as WrapperEntityHolder
    }

    private fun create(
        sourceModel: ISourceModel,
        content: BaseEntityHolder,
        allWrappers: List<String>,
        allBaseModels: Map<String, BaseModelHolder>
    ): BaseEntityHolder {

        content.reducesModels = createReduceModels(sourceModel, content, allWrappers, allBaseModels)
        content.abstractParts = sourceModel.abstractParts
        content.comment = sourceModel.commentAnnotation?.comment ?: arrayOf()
        content.deprecated = sourceModel.deprecatedAnnotation?.let { DeprecatedModel(it) }

        addBasedOn(sourceModel, allBaseModels, content)

        parseQueries(sourceModel, content)
        parseFields(sourceModel, content, allWrappers, allBaseModels)

        val docId = sourceModel.docIdAnnotation
        val docIdSegments: MutableList<DocIdSegmentHolder> = mutableListOf()

        sourceModel.relevantStaticFunctions.forEach {
            if (it.docIdSegment != null) {
                docIdSegments.add(DocIdSegmentHolder(it))
            }
            if (it.generateAccessor != null) {
                content.generateAccessors.add(
                    CblGenerateAccessorHolder(
                        content.sourceClazzTypeName, it, null
                    )
                )
            }
        }
        sourceModel.relevantStaticFields.forEach {
            if (it.generateAccessor != null) {
                content.generateAccessors.add(
                    CblGenerateAccessorHolder(
                        content.sourceClazzTypeName, null, it
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
        content: BaseEntityHolder,
        allWrappers: List<String>,
        allBaseModels: Map<String, BaseModelHolder>
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
            }
        }
    }

    private fun parseFields(
        sourceModel: ISourceModel,
        content: BaseEntityHolder,
        allWrappers: List<String>,
        allBaseModels: Map<String, BaseModelHolder>
    ) {

        for (cblField in sourceModel.fieldAnnotations) {

            if (cblField.readonly) {
                content.fieldConstants[cblField.name] = CblConstantHolder(cblField)
            } else {
                val cblFieldHolder = CblFieldHolder(cblField, allWrappers)
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
