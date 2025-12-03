package com.schwarz.crystalcore.model.source

import com.schwarz.crystalcore.ILogger

interface ISourceModel<T> : IClassModel<T> {
    val entityAnnotation: ISourceEntity?

    val fullQualifiedName: String

    val mapWrapperAnnotation: ISourceMapWrapper?

    val commentAnnotation: ISourceComment?

    val deprecatedSource: ISourceDeprecated?

    val docIdAnnotation: ISourceDocId?

    val basedOnAnnotation: ISourceBasedOn?

    val relevantStaticFunctions: List<SourceMemberFunction>

    val relevantStaticFields: List<SourceMemberField>

    val reduceAnnotations: List<ISourceReduce>

    val fieldAnnotations: List<ISourceField>

    val queryAnnotations: List<ISourceQuery>

    val typeConverterImporter: ISourceTypeConverterImporter?

    val abstractParts: Set<String>

    val typeConverterInterface: TypeConverterInterface?

    fun logError(
        logger: ILogger<T>,
        message: String
    )

    val isPrivateModifier: Boolean

    val isFinalModifier: Boolean

    fun firstNonParameterlessConstructor(): T?

    val isClassSource: Boolean

    val isInterfaceSource: Boolean
}
