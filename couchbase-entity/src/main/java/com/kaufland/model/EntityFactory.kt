package com.kaufland.model

import com.kaufland.model.accessor.CblGenerateAccessorHolder
import com.kaufland.model.entity.BaseEntityHolder
import com.kaufland.model.entity.BaseModelHolder
import com.kaufland.model.entity.EntityHolder
import com.kaufland.model.entity.WrapperEntityHolder
import com.kaufland.model.field.CblConstantHolder
import com.kaufland.model.field.CblFieldHolder
import com.kaufland.model.query.CblQueryHolder
import com.kaufland.util.FieldExtractionUtil
import kaufland.com.coachbasebinderapi.*

import javax.lang.model.element.Element

import kaufland.com.coachbasebinderapi.query.Queries
import org.apache.commons.lang3.text.WordUtils
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier

object EntityFactory {

    fun createEntityHolder(cblEntityElement: Element, allWrappers: List<String>, allBaseModels: Map<String, BaseModelHolder>): EntityHolder {
        val annotation = cblEntityElement.getAnnotation(Entity::class.java)
        return create(cblEntityElement, EntityHolder(annotation.database, annotation.modifierOpen, annotation.type), allWrappers, allBaseModels) as EntityHolder
    }

    fun createBaseModelHolder(cblEntityElement: Element, allWrappers: List<String>): BaseModelHolder {
        return create(cblEntityElement, BaseModelHolder(), allWrappers, emptyMap()) as BaseModelHolder
    }

    fun createChildEntityHolder(cblEntityElement: Element, allWrappers: List<String>, allBaseModels: Map<String, BaseModelHolder>): WrapperEntityHolder {
        val annotation = cblEntityElement.getAnnotation(MapWrapper::class.java)
        return create(cblEntityElement, WrapperEntityHolder(annotation.modifierOpen), allWrappers, allBaseModels) as WrapperEntityHolder
    }

    private fun create(cblEntityElement: Element, content: BaseEntityHolder, allWrappers: List<String>, allBaseModels: Map<String, BaseModelHolder>): BaseEntityHolder {

        content.abstractParts = findPossibleOverrides(cblEntityElement)
        content.sourceElement = cblEntityElement
        content.comment = cblEntityElement.getAnnotation(Comment::class.java)?.comment ?: arrayOf()

        val basedOnValue = cblEntityElement.getAnnotation(BasedOn::class.java)?.let { FieldExtractionUtil.typeMirror(it) }

        basedOnValue?.forEach { type ->
            allBaseModels[type.toString()]?.let {
                content.basedOn.add(it)
                content.fieldConstants.putAll(it.fieldConstants)
                content.fields.putAll(it.fields)
                content.generateAccessors.addAll(it.generateAccessors)
                content.queries.addAll(it.queries)
            }
        }

        parseQueries(cblEntityElement, content)
        parseFields(cblEntityElement, content, allWrappers, allBaseModels)
        parseGenerateAccessors(cblEntityElement, content)

        return content

    }

    private fun parseFields(cblEntityElement: Element, content: BaseEntityHolder, allWrappers: List<String>, allBaseModels: Map<String, BaseModelHolder>) {
        val fields = cblEntityElement.getAnnotation(Fields::class.java)

        for (cblField in fields.value) {

            if (cblField.readonly) {
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
