package com.schwarz.crystalksp.model.source

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSType
import com.schwarz.crystalcore.model.source.ISourceDeclaringName
import com.schwarz.crystalcore.model.source.ISourceMapifyable
import com.schwarz.crystalksp.ProcessingContext
import com.schwarz.crystalksp.util.getArgument

class SourceMapifyable(val mapifyableAnnotations: KSAnnotation) : ISourceMapifyable {

    override val valueDeclaringName: ISourceDeclaringName
        get() = ProcessingContext.DeclaringName(mapifyableAnnotations.getArgument<KSType>("value")!!.declaration)
}
