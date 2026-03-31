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

    /**
     * Source files that this model originates from.
     * KSP: List<KSFile>, KAPT: emptyList().
     * Uses List<Any> to keep crystal-map-core free from KSP dependencies.
     */
    val originatingFiles: List<Any>
        get() = emptyList()

    fun logError(
        logger: ILogger<T>,
        message: String,
    )

    val isPrivateModifier: Boolean

    val isFinalModifier: Boolean

    fun firstNonParameterlessConstructor(): T?

    val isClassSource: Boolean

    val isInterfaceSource: Boolean
}
