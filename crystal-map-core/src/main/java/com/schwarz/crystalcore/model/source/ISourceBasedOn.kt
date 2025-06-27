package com.schwarz.crystalcore.model.source

import com.schwarz.crystalapi.BasedOn

interface ISourceBasedOn {

    val basedOnAnnotation: BasedOn?

    val basedOnFullQualifiedNames : List<String>
}