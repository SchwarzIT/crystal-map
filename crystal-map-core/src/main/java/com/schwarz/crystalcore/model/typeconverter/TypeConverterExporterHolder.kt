package com.schwarz.crystalcore.model.typeconverter

import com.schwarz.crystalcore.model.source.ISourceModel

class TypeConverterExporterHolder<T>(
    val sourceElement: ISourceModel<T>,
) {
    val name: String get() = sourceElement.sourceClazzSimpleName
    val sourcePackageName get() = sourceElement.sourcePackage
}
