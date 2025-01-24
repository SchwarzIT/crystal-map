package com.schwarz.crystalprocessor.model.typeconverter

import com.schwarz.crystalapi.ClassNameDefinition
import com.schwarz.crystalapi.ITypeConverterExporter
import com.schwarz.crystalapi.TypeConverterImportable
import com.schwarz.crystalapi.TypeConverterImporter
import com.schwarz.crystalprocessor.util.FieldExtractionUtil
import com.squareup.kotlinpoet.ClassName
import com.sun.tools.javac.code.Symbol
import kotlinx.metadata.KmClassifier
import kotlinx.metadata.KmTypeProjection
import kotlinx.metadata.isNullable
import javax.lang.model.element.Element

object TypeConverterHolderFactory {

    fun typeConverterHolder(sourceElement: Element): TypeConverterHolder {
        val sourceTypeElement = sourceElement as Symbol.ClassSymbol
        val sourcePackageName = sourceTypeElement.packge().toString()
        val className = sourceTypeElement.simpleName.toString()
        val typeConverterKmType = sourceElement.getTypeConverterInterface()!!
        val (domainClassType, mapClassType) = typeConverterKmType.arguments
        return TypeConverterHolder(
            ClassName(sourcePackageName, className),
            ClassName(sourcePackageName, className + "Instance"),
            domainClassType.resolveToString().toClassName(),
            mapClassType.resolveToString().toClassName(),
            mapClassType.getGenericClassNames()
        )
    }

    fun importedTypeConverterHolders(element: Element): List<ImportedTypeConverterHolder> {
        val annotation = element.getAnnotation(TypeConverterImporter::class.java)
        val typeConverterExporterClassName = FieldExtractionUtil.typeMirror(annotation).first().toString()
        val typeConverterExporterClazz = javaClass.classLoader.loadClass(typeConverterExporterClassName)
        val importables = (typeConverterExporterClazz.constructors[0].newInstance() as ITypeConverterExporter)
            .typeConverterImportables
        return importables.map { importedTypeConverterHolder(it) }
    }

    private fun importedTypeConverterHolder(typeConverterImportable: TypeConverterImportable) =
        ImportedTypeConverterHolder(
            typeConverterImportable.typeConverterInstanceClassName.toClassName(),
            typeConverterImportable.domainClassName.toClassName(),
            typeConverterImportable.mapClassName.toClassName(),
            typeConverterImportable.generics
        )

    private fun ClassNameDefinition.toClassName() = ClassName(packageName, className)

    private fun String.toClassName(): ClassName = split('.').let {
        ClassName(it.subList(0, it.size - 1).joinToString("."), it.last())
    }

    private fun KmTypeProjection.resolveToString(): String {
        val classifier = type!!.classifier as KmClassifier.Class
        val typeName = classifier.name.replace('/', '.')
        return typeName
    }

    private fun KmTypeProjection.getGenericClassNames(): List<ClassNameDefinition> =
        type!!.arguments.fold(emptyList()) { classNameDefinitions, generic ->
            val type = generic.resolveToString().toClassName()
            classNameDefinitions + ClassNameDefinition(
                type.packageName,
                type.simpleName,
                generic.getGenericClassNames(),
                nullable = generic.type!!.isNullable
            )
        }
}
