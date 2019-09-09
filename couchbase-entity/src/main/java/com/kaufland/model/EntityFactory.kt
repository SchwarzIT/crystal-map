package com.kaufland.model

import com.kaufland.model.entity.BaseEntityHolder
import com.kaufland.model.entity.EntityHolder
import com.kaufland.model.entity.WrapperEntityHolder
import com.kaufland.model.field.CblConstantHolder
import com.kaufland.model.field.CblFieldHolder

import javax.lang.model.element.Element

import kaufland.com.coachbasebinderapi.Entity
import kaufland.com.coachbasebinderapi.Field
import kaufland.com.coachbasebinderapi.Fields

object EntityFactory {

    fun createEntityHolder(cblEntityElement: Element, mapWrapper: Boolean): EntityHolder {
        return create(cblEntityElement, EntityHolder(cblEntityElement.getAnnotation(Entity::class.java).database), mapWrapper) as EntityHolder
    }

    fun createChildEntityHolder(cblEntityElement: Element, mapWrapper: Boolean): WrapperEntityHolder {

        return create(cblEntityElement, WrapperEntityHolder(), mapWrapper) as WrapperEntityHolder
    }

    private fun create(cblEntityElement: Element, content: BaseEntityHolder, mapWrapper: Boolean): BaseEntityHolder {

        content.sourceElement = cblEntityElement

        val fields = cblEntityElement.getAnnotation(Fields::class.java)


        for (cblField in fields.value) {

            if (cblField == null) {
                continue
            }

            if (cblField!!.readonly) {
                content.fieldConstants.add(CblConstantHolder(cblField))
            } else {
                val cblFieldHolder = CblFieldHolder(cblField, mapWrapper)
                content.fields.add(cblFieldHolder)
            }
        }

        return content

    }
}
