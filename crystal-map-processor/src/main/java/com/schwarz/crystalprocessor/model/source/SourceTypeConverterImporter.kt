package com.schwarz.crystalprocessor.model.source

import com.schwarz.crystalapi.TypeConverterImporter
import com.schwarz.crystalcore.model.source.ISourceTypeConverterImporter
import com.schwarz.crystalprocessor.util.FieldExtractionUtil

class SourceTypeConverterImporter(override val typeConverterImporterAnnotation: TypeConverterImporter) : ISourceTypeConverterImporter {

    private val typeMirrorExporter = FieldExtractionUtil.typeMirror(typeConverterImporterAnnotation)

    override val exporterClassName: String
        get() = typeMirrorExporter.first().toString()
}
