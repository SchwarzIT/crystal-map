package com.kaufland.model

import com.kaufland.model.accessor.CblGenerateAccessorHolder
import com.kaufland.model.entity.BaseEntityHolder
import com.kaufland.model.entity.EntityHolder
import com.kaufland.model.entity.WrapperEntityHolder
import com.kaufland.model.field.CblConstantHolder
import com.kaufland.model.field.CblFieldHolder
import com.kaufland.model.query.CblQueryHolder

import javax.lang.model.element.Element

import kaufland.com.coachbasebinderapi.Entity
import kaufland.com.coachbasebinderapi.Fields
import kaufland.com.coachbasebinderapi.GenerateAccessor
import kaufland.com.coachbasebinderapi.query.Queries
import org.apache.commons.lang3.text.WordUtils
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

        parseQueries(cblEntityElement, content)
        parseFields(cblEntityElement, content, allWrappers)
        parseGenerateAccessors(cblEntityElement, content)

        return content

    }

    private fun parseFields(cblEntityElement: Element, content: BaseEntityHolder, allWrappers: List<String>) {
        val fields = cblEntityElement.getAnnotation(Fields::class.java)


        for (cblField in fields.value) {

            if (cblField == null) {
                continue
            }

            if (cblField!!.readonly) {
                content.fieldConstants[cblField.name] = CblConstantHolder(cblField)
            } else {
                val cblFieldHolder = CblFieldHolder(cblField, allWrappers)
                content.fields[cblField.name] = cblFieldHolder
            }
        }
    }

    private fun parseGenerateAccessors(cblEntityElement: Element, content: BaseEntityHolder){

        for (childElement in cblEntityElement.enclosedElements) {
            if(childElement.modifiers.contains(Modifier.STATIC)){
                if(childElement.kind == ElementKind.CLASS && childElement.simpleName.toString() == "Companion"){
                    for (companionMembers in childElement.enclosedElements) {
                        addIfAnnotationIsPresent(content.sourceClazzSimpleName, companionMembers, content.generateAccessors)
                    }
                    continue
                }
                addIfAnnotationIsPresent(content.sourceClazzSimpleName, childElement, content.generateAccessors)
            }
        }
    }

    private fun addIfAnnotationIsPresent(className: String, companionMembers: Element, generateAccessors: MutableList<CblGenerateAccessorHolder>) {
        if (companionMembers.getAnnotation(GenerateAccessor::class.java) != null) {
            generateAccessors.add(CblGenerateAccessorHolder(className, companionMembers))
        }
    }

    private fun parseQueries(cblEntityElement: Element, content: BaseEntityHolder) {
        val queries = cblEntityElement.getAnnotation(Queries::class.java) ?: return

        for (cblQuery in queries.value) {

            if (cblQuery == null) {
                continue
            }

            content.queries.add(CblQueryHolder(cblQuery))
        }
    }

    private fun findPossibleOverrides(cblEntityElement: Element): HashSet<String> {
        var abstractSet = HashSet<String>()
        for (enclosedElement in cblEntityElement.enclosedElements) {
            if (enclosedElement.modifiers.contains(Modifier.ABSTRACT) && (enclosedElement.kind == ElementKind.FIELD || enclosedElement.kind == ElementKind.METHOD)) {
                var name = enclosedElement.simpleName.toString()
                if (name.startsWith("set")) {
                    abstractSet.add(WordUtils.uncapitalize(name.replace("set", "")))
                } else if (name.startsWith("get")) {
                    abstractSet.add(WordUtils.uncapitalize(name.replace("get", "")))
                } else {
                    abstractSet.add(name)
                }
            }
        }
        return abstractSet
    }
}
