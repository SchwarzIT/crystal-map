package com.schwarz.crystalprocessor.validation.model

import com.schwarz.crystalprocessor.Logger
import com.sun.tools.javac.code.Symbol
import com.schwarz.crystalapi.Entity
import com.schwarz.crystalapi.Fields
import com.schwarz.crystalapi.ITypeConverter
import com.schwarz.crystalapi.MapWrapper
import com.schwarz.crystalapi.TypeConverter
import com.schwarz.crystalapi.TypeConverterExporter
import com.schwarz.crystalapi.TypeConverterImporter
import com.schwarz.crystalprocessor.model.typeconverter.getTypeConverterInterface
import java.util.ArrayList
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier

object PreModelValidation {

    @Throws(ClassNotFoundException::class)
    fun validate(entityElement: Element, logger: Logger) {
        if (entityElement.getAnnotation(Entity::class.java) != null || entityElement.getAnnotation(
                MapWrapper::class.java
            ) != null
        ) {
            if (entityElement.modifiers.contains(Modifier.PRIVATE)) {
                logger.error(Entity::class.java.simpleName + " can not be private", entityElement)
            }
            if (entityElement.modifiers.contains(Modifier.FINAL)) {
                logger.error(Entity::class.java.simpleName + " can not be final", entityElement)
            }
        }

        val fields = entityElement.getAnnotation(Fields::class.java)

        val names = ArrayList<String>()

        for (fieldAnnotation in fields.value) {
            if (names.contains(fieldAnnotation.name)) {
                logger.warn("duplicated field name", entityElement)
            }

            if (fieldAnnotation.readonly && fieldAnnotation.defaultValue.isEmpty()) {
                logger.warn("defaultValue should not be empty for readonly fields", entityElement)
            }
            names.add(fieldAnnotation.name)
        }

        for (member in entityElement.enclosedElements) {
            if (member.kind == ElementKind.CONSTRUCTOR) {
                val constructor = member as Symbol.MethodSymbol

                if (constructor.parameters.size != 0) {
                    logger.error(
                        Entity::class.java.simpleName + " should not have a constructor",
                        member
                    )
                }
            }
        }
    }

    fun validateTypeConverter(typeConverterElement: Element, logger: Logger) {
        if (typeConverterElement.modifiers.contains(Modifier.PRIVATE)) {
            logger.error(
                TypeConverter::class.java.simpleName + " can not be private",
                typeConverterElement
            )
        }
        if (typeConverterElement.modifiers.contains(Modifier.FINAL)) {
            logger.error(
                TypeConverter::class.java.simpleName + " can not be final",
                typeConverterElement
            )
        }
        if (typeConverterElement.kind != ElementKind.CLASS) {
            logger.error(
                "Only classes can be annotated with ${TypeConverter::class.simpleName}",
                typeConverterElement
            )
        }
        if (typeConverterElement.getTypeConverterInterface() == null) {
            logger.error(
                "Class annotated with ${TypeConverter::class.simpleName} must implement the ${ITypeConverter::class.simpleName} interface",
                typeConverterElement
            )
        }
    }

    fun validateTypeConverterExporter(element: Element, logger: Logger) {
        if (element !is Symbol.ClassSymbol || !element.isInterface) {
            logger.error(
                "${TypeConverterExporter::class.simpleName} annotation has to be on an interface",
                element
            )
        }
    }

    fun validateTypeConverterImporter(element: Element, logger: Logger) {
        if (element !is Symbol.ClassSymbol || !element.isInterface) {
            logger.error(
                "${TypeConverterImporter::class.simpleName} annotation has to be on an interface",
                element
            )
        }
    }
}
