package com.kaufland.model.source

import com.kaufland.model.entity.ReducedModelHolder
import com.kaufland.util.JavaAnnotationUtil
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import kaufland.com.coachbasebinderapi.BasedOn
import kaufland.com.coachbasebinderapi.DocId
import kaufland.com.coachbasebinderapi.Entity
import kaufland.com.coachbasebinderapi.Field
import kaufland.com.coachbasebinderapi.Reduce
import kaufland.com.coachbasebinderapi.query.Query

data class ReducedSourceModel(
    private val sourceModel: ISourceModel,
    private val reducedModelHolder: ReducedModelHolder
) :
    ISourceModel by sourceModel,
    IClassModel by sourceModel {

    override val sourceClazzSimpleName: String =
        "${reducedModelHolder.namePrefix}${sourceModel.sourceClazzSimpleName.capitalize()}"

    override val sourcePackage: String = sourceModel.sourcePackage

    override val entityAnnotation: Entity? = sourceModel.entityAnnotation?.let {
        JavaAnnotationUtil.createReadOnlyCopyOfEntityAnnotation(it)
    }

    override val sourceClazzTypeName: TypeName = ClassName(sourcePackage, sourceClazzSimpleName)

    override val basedOnAnnotation: BasedOn? = if (reducedModelHolder.includeBasedOn) sourceModel.basedOnAnnotation else null

    override val reduceAnnotations: List<Reduce> = emptyList()
    override val fieldAnnotations: List<Field> =
        sourceModel.fieldAnnotations.toMutableList().mapNotNull {
            if (reducedModelHolder.includedElements.contains(it.name).not()) {
                null
            } else it
        }

    override val docIdAnnotation: DocId? =
        if (reducedModelHolder.includeDocId) sourceModel.docIdAnnotation else null

    override val queryAnnotations: List<Query> =
        if (reducedModelHolder.includeQueries) sourceModel.queryAnnotations else listOf()

    override val relevantStaticFields: List<SourceMemberField> =
        sourceModel.relevantStaticFields.mapNotNull {
            val docIdSegment = if (reducedModelHolder.includeDocId) it.docIdSegment else null
            val generateAccessor =
                if (reducedModelHolder.includeAccessor) it.generateAccessor else null
            if (docIdSegment != null || generateAccessor != null) {
                SourceMemberField(it.name, it.type, docIdSegment, generateAccessor)
            } else null
        }

    override val relevantStaticFunctions: List<SourceMemberFunction> =
        sourceModel.relevantStaticFunctions.mapNotNull {
            val docIdSegment = if (reducedModelHolder.includeDocId) it.docIdSegment else null
            val generateAccessor =
                if (reducedModelHolder.includeAccessor) it.generateAccessor else null
            if (docIdSegment != null || generateAccessor != null) {
                SourceMemberFunction(it.name, it.isSuspend, it.returnTypeName, it.parameters, docIdSegment, generateAccessor)
            } else null
        }
}
