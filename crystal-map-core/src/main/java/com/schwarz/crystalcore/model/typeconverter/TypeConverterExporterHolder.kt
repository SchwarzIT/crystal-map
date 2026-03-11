package com.schwarz.crystalcore.model.typeconverter

import com.schwarz.crystalcore.model.source.ISourceModel

class TypeConverterExporterHolder<T>(
    sourceElement: ISourceModel<T>,
) {
    val name: String = sourceElement.sourceClazzSimpleName
    val sourcePackageName: String = sourceElement.sourcePackage
    val originatingFiles: List<Any> = sourceElement.originatingFiles
}
