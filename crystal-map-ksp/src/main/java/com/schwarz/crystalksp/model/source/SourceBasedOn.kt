package com.schwarz.crystalksp.model.source

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSType
import com.schwarz.crystalcore.model.source.ISourceBasedOn
import com.schwarz.crystalksp.util.getArgument
import com.squareup.kotlinpoet.ksp.toTypeName

class SourceBasedOn(
    private val basedOnAnnotation: KSAnnotation,
) : ISourceBasedOn {
    private val basedOnValues =
        basedOnAnnotation.let {
            basedOnAnnotation.getArgument<List<KSType>>("value")
        }

    override val basedOnFullQualifiedNames =
        basedOnValues?.map { it.toTypeName().toString() } ?: listOf()
}
