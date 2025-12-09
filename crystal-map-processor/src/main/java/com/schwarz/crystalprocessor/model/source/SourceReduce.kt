package com.schwarz.crystalprocessor.model.source

import com.schwarz.crystalapi.Reduce
import com.schwarz.crystalcore.model.source.ISourceReduce

class SourceReduce(private val reduceAnnotation: Reduce) : ISourceReduce {
    override val namePrefix: String = reduceAnnotation.namePrefix
    override val include: Array<String> = reduceAnnotation.include
    override val includeQueries: Boolean = reduceAnnotation.includeQueries
    override val includeAccessors: Boolean = reduceAnnotation.includeAccessors
    override val includeDocId: Boolean = reduceAnnotation.includeDocId
    override val includeBasedOn: Boolean = reduceAnnotation.includeBasedOn
}
