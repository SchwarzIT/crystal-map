package com.schwarz.crystalcore.model.typeconverter

import com.schwarz.crystalapi.ClassNameDefinition
import com.schwarz.crystalapi.ITypeConverterExporter
import com.schwarz.crystalapi.TypeConverterImportable
import com.schwarz.crystalcore.model.source.ISourceModel
import com.squareup.kotlinpoet.ClassName
import kotlin.metadata.KmClassifier
import kotlin.metadata.KmTypeProjection
import kotlin.metadata.isNullable

object TypeConverterHolderFactory {

    fun <T>typeConverterHolder(source: ISourceModel<T>): TypeConverterHolder {
        val sourcePackageName = source.sourcePackage
        val className = source.sourceClazzSimpleName
        val typeConverter = source.typeConverterInterface!!

        return TypeConverterHolder(
            ClassName(sourcePackageName, className),
            ClassName(sourcePackageName, className + "Instance"),
            typeConverter.domainClassTypeName,
            typeConverter.mapClassTypeName,
            typeConverter.genericTypeNames
        )
    }

    fun <T>importedTypeConverterHolders(source: ISourceModel<T>): List<ImportedTypeConverterHolder> {
        val importables = source.typeConverterImporter?.typeConverterImportable
        return importables?.map { importedTypeConverterHolder(it) } ?: listOf()
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
