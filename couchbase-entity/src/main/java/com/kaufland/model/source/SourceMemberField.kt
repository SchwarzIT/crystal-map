package com.kaufland.model.source

import com.squareup.kotlinpoet.TypeName
import kaufland.com.coachbasebinderapi.DocIdSegment
import kaufland.com.coachbasebinderapi.GenerateAccessor

data class SourceMemberField(val name: String, val type: TypeName, val docIdSegment: DocIdSegment?, val generateAccessor: GenerateAccessor?)

data class SourceMemberFunction(val name: String, val isSuspend: Boolean, val returnTypeName: TypeName, val parameters: List<Parameter>, val docIdSegment: DocIdSegment?, val generateAccessor: GenerateAccessor?)

data class Parameter(val name: String, val type: TypeName)
