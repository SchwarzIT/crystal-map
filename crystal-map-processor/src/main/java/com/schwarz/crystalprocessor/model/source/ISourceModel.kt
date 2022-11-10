package com.schwarz.crystalprocessor.model.source

import com.schwarz.crystalprocessor.Logger
import com.squareup.kotlinpoet.TypeName
import com.schwarz.crystalapi.BasedOn
import com.schwarz.crystalapi.Comment
import com.schwarz.crystalapi.DocId
import com.schwarz.crystalapi.Entity
import com.schwarz.crystalapi.Field
import com.schwarz.crystalapi.MapWrapper
import com.schwarz.crystalapi.Reduce
import com.schwarz.crystalapi.query.Query
import com.schwarz.crystalapi.deprecated.Deprecated

interface ISourceModel : IClassModel {

    val entityAnnotation: Entity?

    val typeName: TypeName

    val mapWrapperAnnotation: MapWrapper?

    val commentAnnotation: Comment?

    val deprecatedAnnotation: Deprecated?

    val docIdAnnotation: DocId?

    val basedOnAnnotation: BasedOn?

    val relevantStaticFunctions: List<SourceMemberFunction>

    val relevantStaticFields: List<SourceMemberField>

    val reduceAnnotations: List<Reduce>

    val fieldAnnotations: List<Field>

    val queryAnnotations: List<Query>

    val abstractParts: Set<String>

    fun logError(logger: Logger, message: String)
}
