package com.kaufland.model

import com.kaufland.model.entity.BaseEntityHolder
import com.kaufland.model.entity.EntityHolder
import com.kaufland.model.entity.WrapperEntityHolder
import com.kaufland.model.field.CblConstantHolder
import com.kaufland.model.field.CblFieldHolder

import javax.lang.model.element.Element

import kaufland.com.coachbasebinderapi.Entity
import kaufland.com.coachbasebinderapi.Fields
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier

object EntityFactory {

    fun createEntityHolder(cblEntityElement: Element, allWrappers: List<String>): EntityHolder {
        return create(cblEntityElement, EntityHolder(cblEntityElement.getAnnotation(Entity::class.java).database), allWrappers) as EntityHolder
    }

    fun createChildEntityHolder(cblEntityElement: Element, allWrappers: List<String>): WrapperEntityHolder {

        return create(cblEntityElement, WrapperEntityHolder(), allWrappers) as WrapperEntityHolder
    }

    private fun create(cblEntityElement: Element, content: BaseEntityHolder, allWrappers: List<String>): BaseEntityHolder {

        content.abstractParts = findPossibleOverrides(cblEntityElement)
        content.sourceElement = cblEntityElement

        val fields = cblEntityElement.getAnnotation(Fields::class.java)


        for (cblField in fields.value) {

            if (cblField == null) {
                continue
            }

            if (cblField!!.readonly) {
                content.fieldConstants.add(CblConstantHolder(cblField))
            } else {
                val cblFieldHolder = CblFieldHolder(cblField, allWrappers)
                content.fields.add(cblFieldHolder)
            }
        }

        return content

    }

    private fun findPossibleOverrides(cblEntityElement: Element): HashSet<String> {
        var abstractSet = HashSet<String>()
        for (enclosedElement in cblEntityElement.enclosedElements) {
            if (enclosedElement.modifiers.contains(Modifier.ABSTRACT) && (enclosedElement.kind == ElementKind.FIELD || enclosedElement.kind == ElementKind.METHOD)) {
                var name = enclosedElement.simpleName.toString()
                if (name.startsWith("set")) {
                    abstractSet.add(name.replace("set", "").toLowerCase())
                } else if (name.startsWith("get")) {
                    abstractSet.add(name.replace("get", "").toLowerCase())
                } else {
                    abstractSet.add(name)
                }
            }
        }
        return abstractSet
    }
}
