package com.schwarz.crystalksp.model.source

import com.google.devtools.ksp.symbol.KSAnnotation
import com.schwarz.crystalapi.Reduce
import com.schwarz.crystalcore.model.source.ISourceReduce
import com.schwarz.crystalksp.util.getArgument

class SourceReduce(val reduceAnnotation: KSAnnotation) : ISourceReduce {
    override val namePrefix: String = reduceAnnotation.getArgument<String>("namePrefix") ?: ""
    override val include: Array<String> = reduceAnnotation.getArgument<List<String>>("include")?.toTypedArray() ?: arrayOf()
    override val includeQueries: Boolean = reduceAnnotation.getArgument<Boolean>("includeQueries") ?: false
    override val includeAccessors: Boolean = reduceAnnotation.getArgument<Boolean>("includeAccessors") ?: false
    override val includeDocId: Boolean = reduceAnnotation.getArgument<Boolean>("includeDocId") ?: false
    override val includeBasedOn: Boolean = reduceAnnotation.getArgument<Boolean>("includeBasedOn") ?: false
}
