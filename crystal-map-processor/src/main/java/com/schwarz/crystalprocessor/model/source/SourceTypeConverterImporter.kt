package com.schwarz.crystalprocessor.model.source

import com.schwarz.crystalapi.ITypeConverterExporter
import com.schwarz.crystalapi.TypeConverterImportable
import com.schwarz.crystalapi.TypeConverterImporter
import com.schwarz.crystalcore.model.source.ISourceTypeConverterImporter
import com.schwarz.crystalprocessor.util.FieldExtractionUtil

class SourceTypeConverterImporter(private val typeConverterImporterAnnotation: TypeConverterImporter) : ISourceTypeConverterImporter {
    private val typeMirrorExporter = FieldExtractionUtil.typeMirror(typeConverterImporterAnnotation)

    private val exporterClassName: String
        get() = typeMirrorExporter.first().toString()
    override val typeConverterImportable: List<TypeConverterImportable>
        get() {
            val typeConverterExporterClazz = javaClass.classLoader.loadClass(exporterClassName)
            return (typeConverterExporterClazz.constructors[0].newInstance() as ITypeConverterExporter).typeConverterImportables
        }
}
