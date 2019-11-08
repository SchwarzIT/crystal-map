package com.kaufland.model

import com.kaufland.model.entity.BaseEntityHolder
import com.kaufland.model.entity.EntityHolder
import com.kaufland.model.entity.WrapperEntityHolder
import com.kaufland.model.field.CblConstantHolder
import com.kaufland.model.field.CblFieldHolder

import javax.lang.model.element.Element

import kaufland.com.coachbasebinderapi.Entity
import kaufland.com.coachbasebinderapi.Fields

object EntityFactory {

    fun createEntityHolder(cblEntityElement: Element, allWrappers: List<String>): EntityHolder {
        return create(cblEntityElement, EntityHolder(cblEntityElement.getAnnotation(Entity::class.java).database), allWrappers) as EntityHolder
    }

    fun createChildEntityHolder(cblEntityElement: Element, allWrappers: List<String>): WrapperEntityHolder {

        return create(cblEntityElement, WrapperEntityHolder(), allWrappers) as WrapperEntityHolder
    }

    private fun create(cblEntityElement: Element, content: BaseEntityHolder, allWrappers: List<String>): BaseEntityHolder {

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
}
