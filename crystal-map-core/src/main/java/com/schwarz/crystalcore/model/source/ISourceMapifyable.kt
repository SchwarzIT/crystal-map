package com.schwarz.crystalcore.model.source

import com.schwarz.crystalapi.mapify.Mapifyable

interface ISourceMapifyable {

    val mapifyableAnnotations: Mapifyable

    val valueDeclaringName: ISourceDeclaringName
}
