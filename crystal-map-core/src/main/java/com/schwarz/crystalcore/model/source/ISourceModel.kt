package com.schwarz.crystalcore.model.source

import com.squareup.kotlinpoet.TypeName
import com.schwarz.crystalapi.BasedOn
import com.schwarz.crystalapi.Comment
import com.schwarz.crystalapi.DocId
import com.schwarz.crystalapi.Entity
import com.schwarz.crystalapi.MapWrapper
import com.schwarz.crystalcore.ILogger

interface ISourceModel<T> : IClassModel {

    val entityAnnotation: Entity?

    val typeName: TypeName

    val fullQualifiedName: String

    val mapWrapperAnnotation: MapWrapper?

    val commentAnnotation: Comment?

    val deprecatedSource: ISourceDeprecated?

    val docIdAnnotation: DocId?

    val basedOnAnnotation: ISourceBasedOn?

    val relevantStaticFunctions: List<SourceMemberFunction>

    val relevantStaticFields: List<SourceMemberField>

    val reduceAnnotations: List<ISourceReduce>

    val fieldAnnotations: List<ISourceField>

    val queryAnnotations: List<ISourceQuery>

    val typeConverterImporter: ISourceTypeConverterImporter?

    val abstractParts: Set<String>

    val kotlinMetadata: Metadata?

    fun logError(logger: ILogger<T>, message: String)

    val isPrivateModifier: Boolean

    val isFinalModifier: Boolean

    fun firstNonParameterlessConstructor() : T?

    val isClassSource: Boolean

    val isInterfaceSource: Boolean

    val source: T
}
