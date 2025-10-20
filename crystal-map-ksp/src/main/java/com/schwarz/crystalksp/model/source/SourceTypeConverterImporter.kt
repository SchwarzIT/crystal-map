package com.schwarz.crystalksp.model.source

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSType
import com.schwarz.crystalapi.ClassNameDefinition
import com.schwarz.crystalapi.TypeConverterImportable
import com.schwarz.crystalapi.converterexport.ImportableConverters
import com.schwarz.crystalcore.model.source.ISourceTypeConverterImporter
import com.schwarz.crystalksp.ProcessingContext
import com.schwarz.crystalksp.util.getAnnotation
import com.schwarz.crystalksp.util.getArgument
import com.squareup.kotlinpoet.ksp.toClassName

class SourceTypeConverterImporter(val typeConverterImporterAnnotation: KSAnnotation) : ISourceTypeConverterImporter {

    private val typeMirrorExporter = typeConverterImporterAnnotation.getArgument<KSType>("typeConverterExporter")

    private val exporterClassName: String
        get() = typeMirrorExporter!!.toClassName().toString()
    override val typeConverterImportable: List<TypeConverterImportable>
        get() {
            val result = arrayListOf<TypeConverterImportable>()
            ProcessingContext.resolver.getClassDeclarationByName(exporterClassName)?.let {
                it.getAnnotation(ImportableConverters::class)?.getArgument<List<KSAnnotation>>("value")?.forEach {
                    val typeConverterInstance = it.getArgument<KSAnnotation>("typeConverterInstanceTargetDefinition")
                    val domainTargetDefinition = it.getArgument<KSAnnotation>("domainTargetDefinition")
                    val mapTargetDefinition = it.getArgument<KSAnnotation>("mapTargetDefinition")

                    result.add(
                        TypeConverterImportable(
                            ClassNameDefinition(typeConverterInstance?.getArgument<String>("pkg") ?: "", typeConverterInstance?.getArgument<String>("name") ?: ""),
                            ClassNameDefinition(domainTargetDefinition?.getArgument<String>("pkg") ?: "", domainTargetDefinition?.getArgument<String>("name") ?: ""),
                            ClassNameDefinition(mapTargetDefinition?.getArgument<String>("pkg") ?: "", mapTargetDefinition?.getArgument<String>("name") ?: ""),
                            it.getArgument<List<KSAnnotation>>("genericsTargetDefinitions")?.map {
                                ClassNameDefinition(it.getArgument<String>("pkg") ?: "", it.getArgument<String>("name") ?: "")
                            } ?: listOf()
                        )
                    )
                }
            }
            return result
        }
}
