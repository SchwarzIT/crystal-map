package com.schwarz.crystalcore.model.source

import com.schwarz.crystalapi.Entity
import com.schwarz.crystalcore.model.entity.ReducedModelHolder
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName

data class ReducedSourceModel<T>(
    private val sourceModel: ISourceModel<T>,
    private val reducedModelHolder: ReducedModelHolder<T>
) :
    ISourceModel<T> by sourceModel {
    override val sourceClazzSimpleName: String =
        "${reducedModelHolder.namePrefix}${sourceModel.sourceClazzSimpleName.capitalize()}"

    override val sourcePackage: String = sourceModel.sourcePackage

    override val entityAnnotation: ISourceEntity? =
        sourceModel.entityAnnotation?.let {
            object : ISourceEntity {
                override val modifierOpen: Boolean
                    get() = it.modifierOpen
                override val type: Entity.Type
                    get() = Entity.Type.READONLY
                override val database: String
                    get() = it.database
            }
        }

    override val sourceClazzTypeName: TypeName = ClassName(sourcePackage, sourceClazzSimpleName)

    override val basedOnAnnotation: ISourceBasedOn? = if (reducedModelHolder.includeBasedOn) sourceModel.basedOnAnnotation else null

    override val reduceAnnotations: List<ISourceReduce> = emptyList()
    override val fieldAnnotations: List<ISourceField> =
        sourceModel.fieldAnnotations.toMutableList().mapNotNull {
            if (reducedModelHolder.includedElements.contains(it.name).not()) {
                null
            } else {
                it
            }
        }

    override val docIdAnnotation: ISourceDocId? =
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
