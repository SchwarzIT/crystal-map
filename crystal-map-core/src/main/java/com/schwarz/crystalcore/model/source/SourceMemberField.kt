package com.schwarz.crystalcore.model.source

import com.squareup.kotlinpoet.TypeName

data class SourceMemberField(
    val name: String,
    val type: TypeName,
    val docIdSegment: ISourceDocIdSegment?,
    val generateAccessor: ISourceGenerateAccessor?,
)

data class SourceMemberFunction(
    val name: String,
    val isSuspend: Boolean,
    val returnTypeName: TypeName,
    val parameters: List<Parameter>,
    val docIdSegment: ISourceDocIdSegment?,
    val generateAccessor: ISourceGenerateAccessor?,
)

data class Parameter(
    val name: String,
    val type: TypeName,
)
