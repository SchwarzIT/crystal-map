package com.schwarz.crystalcore.validation.model

import com.schwarz.crystalapi.Entity
import com.schwarz.crystalapi.ITypeConverter
import com.schwarz.crystalapi.TypeConverter
import com.schwarz.crystalapi.TypeConverterExporter
import com.schwarz.crystalapi.TypeConverterImporter
import com.schwarz.crystalcore.ILogger
import com.schwarz.crystalcore.model.source.ISourceModel
import com.schwarz.crystalcore.model.typeconverter.getTypeConverterInterface

object PreModelValidation {

    @Throws(ClassNotFoundException::class)
    fun <T>validate(entityElement: ISourceModel<T>, logger: ILogger<T>) {
        if (entityElement.entityAnnotation != null || entityElement.mapWrapperAnnotation != null
        ) {
            if (entityElement.isPrivateModifier) {
                logger.error(Entity::class.java.simpleName + " can not be private", entityElement.source)
            }
            if (entityElement.isFinalModifier) {
                logger.error(Entity::class.java.simpleName + " can not be final", entityElement.source)
            }
        }

        val fields = entityElement.fieldAnnotations

        val names = ArrayList<String>()

        for (fieldAnnotation in fields.map { it.fieldAnnotation }) {
            if (names.contains(fieldAnnotation.name)) {
                logger.warn("duplicated field name", entityElement.source)
            }

            if (fieldAnnotation.readonly && fieldAnnotation.defaultValue.isEmpty()) {
                logger.warn("defaultValue should not be empty for readonly fields", entityElement.source)
            }
            names.add(fieldAnnotation.name)
        }

        entityElement.firstNonParameterlessConstructor()?.let {
            logger.error(
                Entity::class.java.simpleName + " should not have a constructor",
                it
            )
        }
    }

    fun <T>validateTypeConverter(typeConverterElement: ISourceModel<T>, logger: ILogger<T>) {
        if (typeConverterElement.isPrivateModifier) {
            logger.error(
                TypeConverter::class.java.simpleName + " can not be private",
                typeConverterElement.source
            )
        }
        if (typeConverterElement.isFinalModifier) {
            logger.error(
                TypeConverter::class.java.simpleName + " can not be final",
                typeConverterElement.source
            )
        }
        if (typeConverterElement.isClassSource.not()) {
            logger.error(
                "Only classes can be annotated with ${TypeConverter::class.simpleName}",
                typeConverterElement.source
            )
        }
        if (typeConverterElement.kotlinMetadata?.getTypeConverterInterface() == null) {
            logger.error(
                "Class annotated with ${TypeConverter::class.simpleName} must implement the ${ITypeConverter::class.simpleName} interface",
                typeConverterElement.source
            )
        }
    }

    fun <T>validateTypeConverterExporter(element: ISourceModel<T>, logger: ILogger<T>) {
        if (element.isInterfaceSource) {
            logger.error(
                "${TypeConverterExporter::class.simpleName} annotation has to be on an interface",
                element.source
            )
        }
    }

    fun <T>validateTypeConverterImporter(element: ISourceModel<T>, logger: ILogger<T>) {
        if (element.isInterfaceSource) {
            logger.error(
                "${TypeConverterImporter::class.simpleName} annotation has to be on an interface",
                element.source
            )
        }
    }
}
