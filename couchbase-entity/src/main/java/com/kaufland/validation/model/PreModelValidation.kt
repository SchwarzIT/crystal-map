package com.kaufland.validation.model

import com.kaufland.Logger
import com.sun.tools.javac.code.Symbol
import kaufland.com.coachbasebinderapi.Entity
import kaufland.com.coachbasebinderapi.Fields
import kaufland.com.coachbasebinderapi.MapWrapper
import java.util.ArrayList
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier

object PreModelValidation {

    @Throws(ClassNotFoundException::class)
    fun validate(entityElement: Element, logger: Logger) {

        if (entityElement.getAnnotation(Entity::class.java) != null || entityElement.getAnnotation(MapWrapper::class.java) != null) {

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
                    logger.error(Entity::class.java.simpleName + " should not have a contructor", member)
                }
            }
        }
    }
}
