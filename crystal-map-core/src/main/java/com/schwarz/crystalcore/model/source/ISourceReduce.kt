package com.schwarz.crystalcore.model.source

interface ISourceReduce {
    val namePrefix: String

    val include: Array<String>

    val includeQueries: Boolean

    val includeAccessors: Boolean

    val includeDocId: Boolean

    val includeBasedOn: Boolean
}
