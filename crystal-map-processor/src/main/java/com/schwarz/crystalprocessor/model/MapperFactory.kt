package com.schwarz.crystalprocessor.model

import com.schwarz.crystalprocessor.model.mapper.MapifyHolder
import com.schwarz.crystalprocessor.model.mapper.MapperHolder
import com.schwarz.crystalprocessor.model.mapper.type.MapifyElementTypeField
import com.schwarz.crystalprocessor.model.mapper.type.MapifyElementTypeGetterSetter
import com.sun.tools.javac.code.Symbol
import com.schwarz.crystalapi.mapify.Mapify
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier

object MapperFactory {

    fun create(env: ProcessingEnvironment, mapperElement: Element): MapperHolder {

        val result = MapperHolder(mapperElement)

        val getterSetterMap = hashMapOf<String, MapifyElementTypeGetterSetter.GetterSetter>()
        for (childElement in mapperElement.enclosedElements) {
            if (childElement.modifiers.contains(Modifier.STATIC)) {
                continue
            }

            if (childElement.kind == ElementKind.FIELD) {
                childElement.getAnnotation(Mapify::class.java)?.apply {
                    result.fields[childElement.simpleName.toString()] = MapifyHolder(
                        MapifyElementTypeField(childElement, this), env
                    )
                }
            }

            if (childElement.kind == ElementKind.METHOD) {
                childElement.getAnnotation(Mapify::class.java)?.let { anno ->
                    childElement.simpleName.toString()?.let {
                        if (it.startsWith("set")) {
                            getterSetterMap.getOrPut(it.substringAfter("set")) {
                                MapifyElementTypeGetterSetter.GetterSetter()
                            }?.apply {
                                setterElement = childElement as Symbol.MethodSymbol?
                                mapify = anno
                            }
                        } else if (it.startsWith("get")) {
                            getterSetterMap.getOrPut(it.substringAfter("get")) {
                                MapifyElementTypeGetterSetter.GetterSetter()
                            }?.apply {
                                getterElement = childElement as Symbol.MethodSymbol?
                                mapify = anno
                            }
                        }
                        null
                    }
                }
            }
        }

        getterSetterMap.filter { it.value.getterElement != null && it.value.setterElement != null }?.forEach {
            result.fields[it.key] = MapifyHolder(MapifyElementTypeGetterSetter(it.value, fieldName = it.key), env)
        }

        return result
    }
}
