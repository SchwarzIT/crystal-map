package com.schwarz.crystalcore.model.source

import com.schwarz.crystalapi.TypeConverterImporter

interface ISourceTypeConverterImporter {

    val typeConverterImporterAnnotation: TypeConverterImporter

    val exporterClassName: String
}