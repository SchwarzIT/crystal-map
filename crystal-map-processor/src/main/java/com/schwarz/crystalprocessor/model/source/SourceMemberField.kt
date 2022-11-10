package com.schwarz.crystalprocessor.model.source

import com.squareup.kotlinpoet.TypeName
import com.schwarz.crystalapi.DocIdSegment
import com.schwarz.crystalapi.GenerateAccessor

data class SourceMemberField(val name: String, val type: TypeName, val docIdSegment: DocIdSegment?, val generateAccessor: GenerateAccessor?)

data class SourceMemberFunction(val name: String, val isSuspend: Boolean, val returnTypeName: TypeName, val parameters: List<Parameter>, val docIdSegment: DocIdSegment?, val generateAccessor: GenerateAccessor?)

data class Parameter(val name: String, val type: TypeName)
