package com.schwarz.crystalprocessor.model.typeconverter

import com.sun.tools.javac.code.Symbol
import javax.lang.model.element.Element

class TypeConverterExporterHolder(val sourceElement: Element) {

    private val sourceTypeElement: Symbol.ClassSymbol = sourceElement as Symbol.ClassSymbol
    private val sourcePackage: Symbol.PackageSymbol = sourceTypeElement.packge()

    val name: String get() = sourceTypeElement.simpleName.toString()
    val sourcePackageName get() = sourcePackage.toString()
}
