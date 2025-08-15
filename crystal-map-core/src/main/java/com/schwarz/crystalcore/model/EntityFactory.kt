package com.schwarz.crystalcore.model

import com.schwarz.crystalcore.model.accessor.CblGenerateAccessorHolder
import com.schwarz.crystalcore.model.deprecated.DeprecatedModel
import com.schwarz.crystalcore.model.entity.BaseEntityHolder
import com.schwarz.crystalcore.model.entity.BaseModelHolder
import com.schwarz.crystalcore.model.entity.EntityHolder
import com.schwarz.crystalcore.model.entity.ReducedModelHolder
import com.schwarz.crystalcore.model.entity.WrapperEntityHolder
import com.schwarz.crystalcore.model.entity.SchemaClassHolder
import com.schwarz.crystalcore.model.field.CblConstantHolder
import com.schwarz.crystalcore.model.field.CblFieldHolder
import com.schwarz.crystalcore.model.id.DocIdHolder
import com.schwarz.crystalcore.model.id.DocIdSegmentHolder
import com.schwarz.crystalcore.model.query.CblQueryHolder
import com.schwarz.crystalcore.model.source.ISourceModel

private const val WRAPPER_SUB_ENTITY_SUFFIX = "Wrapper"
private const val SCHEMA_SUB_ENTITY_SUFFIX = "Schema"
object EntityFactory {

    fun <T>createEntityHolder(
        sourceModel: ISourceModel<T>,
        allWrapperPaths: List<String>,
        allBaseModels: Map<String, BaseModelHolder<T>>
    ): EntityHolder<T> {
        val annotation = sourceModel.entityAnnotation!!
        return create(
            sourceModel,
            EntityHolder(
                annotation.database,
                annotation.modifierOpen,
                annotation.type,
                sourceModel
            ),
            allWrapperPaths,
            allBaseModels,
            WRAPPER_SUB_ENTITY_SUFFIX
        ) as EntityHolder
    }

    fun <T>createWrapperBaseModelHolder(
        sourceModel: ISourceModel<T>,
        allWrapperPaths: List<String>
    ): BaseModelHolder<T> {
        return create(
            sourceModel,
            BaseModelHolder(sourceModel),
            allWrapperPaths,
            emptyMap(),
            WRAPPER_SUB_ENTITY_SUFFIX
        ) as BaseModelHolder
    }

    fun <T>createSchemaBaseModelHolder(
        sourceModel: ISourceModel<T>,
        allSchemaClassPaths: List<String>
    ): BaseModelHolder<T> {
        return create(
            sourceModel,
            BaseModelHolder(sourceModel),
            allSchemaClassPaths,
            emptyMap(),
            SCHEMA_SUB_ENTITY_SUFFIX
        ) as BaseModelHolder
    }

    fun <T>createChildEntityHolder(
        sourceModel: ISourceModel<T>,
        allWrapperPaths: List<String>,
        allBaseModels: Map<String, BaseModelHolder<T>>
    ): WrapperEntityHolder<T> {
        val annotation = sourceModel.mapWrapperAnnotation!!
        return create(
            sourceModel,
            WrapperEntityHolder(annotation.modifierOpen, sourceModel),
            allWrapperPaths,
            allBaseModels,
            WRAPPER_SUB_ENTITY_SUFFIX
        ) as WrapperEntityHolder
    }

    fun <T>createSchemaEntityHolder(
        sourceModel: ISourceModel<T>,
        allSchemaClassPaths: List<String>,
        allBaseModels: Map<String, BaseModelHolder<T>>
    ): SchemaClassHolder<T> = create(
        sourceModel,
        SchemaClassHolder(sourceModel),
        allSchemaClassPaths,
        allBaseModels,
        SCHEMA_SUB_ENTITY_SUFFIX
    ) as SchemaClassHolder

    private fun <T>create(
        sourceModel: ISourceModel<T>,
        content: BaseEntityHolder<T>,
        classPaths: List<String>,
        allBaseModels: Map<String, BaseModelHolder<T>>,
        subEntityNameSuffix: String
    ): BaseEntityHolder<T> {
        content.reducesModels = createReduceModels(sourceModel, content)
        content.abstractParts = sourceModel.abstractParts
        content.comment = sourceModel.commentAnnotation?.comment ?: arrayOf()
        content.deprecated = sourceModel.deprecatedSource?.let { DeprecatedModel(it) }

        addBasedOn(sourceModel, allBaseModels, content)

        parseQueries(sourceModel, content)
        parseFields(sourceModel, content, classPaths, subEntityNameSuffix)

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

    private fun <T>createReduceModels(
        sourceModel: ISourceModel<T>,
        content: BaseEntityHolder<T>
    ): List<ReducedModelHolder<T>> {
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

    fun <T>addBasedOn(
        sourceModel: ISourceModel<T>,
        allBaseModels: Map<String, BaseModelHolder<T>>,
        content: BaseEntityHolder<T>
    ) {
        sourceModel.basedOnAnnotation?.basedOnFullQualifiedNames?.forEach { type ->
            allBaseModels[type]?.let {
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
                addBasedOn<T>(it.sourceElement, allBaseModels, content)
            }
        }
    }

    private fun <T>parseFields(
        sourceModel: ISourceModel<T>,
        content: BaseEntityHolder<T>,
        classPaths: List<String>,
        subEntityNameSuffix: String
    ) {
        for (cblField in sourceModel.fieldAnnotations) {
            if (cblField.readonly) {
                content.fieldConstants[cblField.name] = CblConstantHolder(cblField)
            } else {
                val cblFieldHolder = CblFieldHolder(cblField, classPaths, subEntityNameSuffix)
                content.fields[cblField.name] = cblFieldHolder
            }
        }
    }

    private fun <T>parseQueries(sourceModel: ISourceModel<T>, content: BaseEntityHolder<T>) {
        val queries = sourceModel.queryAnnotations

        for (cblQuery in queries) {
            content.queries.add(CblQueryHolder(cblQuery))
        }
    }
}
