package com.kaufland.model

import com.kaufland.model.mapper.MapifyHolder
import com.kaufland.model.mapper.MapperHolder
import kaufland.com.coachbasebinderapi.mapify.Mapify
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier

object MapperFactory {

    fun create(env: ProcessingEnvironment, mapperElement: Element): MapperHolder {

        val result = MapperHolder(mapperElement)

        for (childElement in mapperElement.enclosedElements) {
            if (childElement.modifiers.contains(Modifier.STATIC)) {
                continue
            }

            if (childElement.kind == ElementKind.FIELD) {
                childElement.getAnnotation(Mapify::class.java)?.apply {
                    result.fields[childElement.simpleName.toString()] = MapifyHolder(childElement, this, env)
                }

            }
        }

        return result
    }
}