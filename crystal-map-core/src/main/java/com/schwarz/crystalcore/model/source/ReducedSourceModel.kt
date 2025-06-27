package com.schwarz.crystalcore.model.source

import com.schwarz.crystalcore.model.entity.ReducedModelHolder
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import com.schwarz.crystalapi.DocId
import com.schwarz.crystalapi.Entity
import com.schwarz.crystalcore.util.JavaAnnotationUtil

data class ReducedSourceModel<T>(
    private val sourceModel: ISourceModel<T>,
    private val reducedModelHolder: ReducedModelHolder<T>
) :
    ISourceModel<T> by sourceModel,
    IClassModel by sourceModel {

    override val sourceClazzSimpleName: String =
        "${reducedModelHolder.namePrefix}${sourceModel.sourceClazzSimpleName.capitalize()}"

    override val sourcePackage: String = sourceModel.sourcePackage

    override val entityAnnotation: Entity? = sourceModel.entityAnnotation?.let {
        JavaAnnotationUtil.createReadOnlyCopyOfEntityAnnotation(it)
    }

    override val sourceClazzTypeName: TypeName = ClassName(sourcePackage, sourceClazzSimpleName)

    override val basedOnAnnotation: ISourceBasedOn? = if (reducedModelHolder.includeBasedOn) sourceModel.basedOnAnnotation else null

    override val reduceAnnotations: List<ISourceReduce> = emptyList()
    override val fieldAnnotations: List<ISourceField> =
        sourceModel.fieldAnnotations.toMutableList().mapNotNull {
            if (reducedModelHolder.includedElements.contains(it.fieldAnnotation.name).not()) {
                null
            } else {
                it
            }
        }

    override val docIdAnnotation: DocId? =
        if (reducedModelHolder.includeDocId) sourceModel.docIdAnnotation else null

    override val queryAnnotations: List<ISourceQuery> =
        if (reducedModelHolder.includeQueries) sourceModel.queryAnnotations else listOf()

    override val relevantStaticFields: List<SourceMemberField> =
        sourceModel.relevantStaticFields.mapNotNull {
            val docIdSegment = if (reducedModelHolder.includeDocId) it.docIdSegment else null
            val generateAccessor =
                if (reducedModelHolder.includeAccessor) it.generateAccessor else null
            if (docIdSegment != null || generateAccessor != null) {
                SourceMemberField(it.name, it.type, docIdSegment, generateAccessor)
            } else {
                null
            }
        }

    override val relevantStaticFunctions: List<SourceMemberFunction> =
        sourceModel.relevantStaticFunctions.mapNotNull {
            val docIdSegment = if (reducedModelHolder.includeDocId) it.docIdSegment else null
            val generateAccessor =
                if (reducedModelHolder.includeAccessor) it.generateAccessor else null
            if (docIdSegment != null || generateAccessor != null) {
                SourceMemberFunction(it.name, it.isSuspend, it.returnTypeName, it.parameters, docIdSegment, generateAccessor)
            } else {
                null
            }
        }
}
