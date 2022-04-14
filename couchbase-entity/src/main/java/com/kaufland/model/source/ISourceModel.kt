package com.kaufland.model.source

import com.kaufland.Logger
import com.squareup.kotlinpoet.TypeName
import kaufland.com.coachbasebinderapi.BasedOn
import kaufland.com.coachbasebinderapi.Comment
import kaufland.com.coachbasebinderapi.DocId
import kaufland.com.coachbasebinderapi.Entity
import kaufland.com.coachbasebinderapi.Field
import kaufland.com.coachbasebinderapi.MapWrapper
import kaufland.com.coachbasebinderapi.Reduce
import kaufland.com.coachbasebinderapi.deprecated.Deprecated
import kaufland.com.coachbasebinderapi.query.Query

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
