package com.kaufland.validation.mapper

import com.kaufland.Logger
import com.kaufland.model.mapper.type.MapifyElementTypeGetterSetter
import com.sun.tools.javac.code.Symbol
import kaufland.com.coachbasebinderapi.Entity
import kaufland.com.coachbasebinderapi.Fields
import kaufland.com.coachbasebinderapi.MapWrapper
import kaufland.com.coachbasebinderapi.mapify.Mapify
import kaufland.com.coachbasebinderapi.mapify.Mapifyable
import java.util.ArrayList
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier

object PreMapperValidation {


    @Throws(ClassNotFoundException::class)
    fun validate(mapperElement: Element, logger: Logger) {

        val getterMap: MutableMap<String, Element> = hashMapOf()
        val setterMap: MutableMap<String, Element> = hashMapOf()
        for (member in mapperElement.enclosedElements) {
            if (member.kind == ElementKind.METHOD && member.getAnnotation(Mapify::class.java) != null) {

                val isGetter: Boolean = member.simpleName.toString().let { it.startsWith("get")}

                val isSetter: Boolean = member.simpleName.toString().let { it.startsWith("set")}

                when{
                    isGetter -> getterMap[member.simpleName.toString().substringAfter("get")] = member
                    isSetter -> setterMap[member.simpleName.toString().substringAfter("set")] = member
                    else -> logger.error("${Mapify::class.java.canonicalName} is only allowed for getters/setters/fields", member)
                }
            }
        }

        hashSetOf(*getterMap.keys.minus(setterMap.keys).toTypedArray(), *setterMap.keys.minus(getterMap.keys).toTypedArray()).forEach {

            println(it)

            val element = getterMap[it] ?: setterMap[it] ?: mapperElement
            logger.error("${Mapifyable::class.java.simpleName} needs to be applied on getter and setter. ", element)
        }

    }
}