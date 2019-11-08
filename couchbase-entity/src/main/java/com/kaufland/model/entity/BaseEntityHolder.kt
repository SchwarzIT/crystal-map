package com.kaufland.model.entity

import com.kaufland.model.field.CblBaseFieldHolder
import com.kaufland.model.field.CblConstantHolder
import com.kaufland.model.field.CblFieldHolder
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import com.sun.tools.javac.code.Symbol

import java.util.ArrayList

import javax.lang.model.element.Element

abstract class BaseEntityHolder {

    val fields: MutableList<CblFieldHolder> = ArrayList()

    val fieldConstants: MutableList<CblConstantHolder> = ArrayList()

    var sourceElement: Element? = null

    val allFields: List<CblBaseFieldHolder>
        get() {
            val allField = ArrayList<CblBaseFieldHolder>()
            allField.addAll(fields)
            allField.addAll(fieldConstants)
            return allField
        }

    val sourceClazzSimpleName: String
        get() = (sourceElement as Symbol.ClassSymbol).simpleName.toString()

    open val entitySimpleName: String
        get() = sourceClazzSimpleName + "Entity"

    val `package`: String
        get() = (sourceElement as Symbol.ClassSymbol).packge().toString()

    val entityTypeName: TypeName
        get() = ClassName(`package`, entitySimpleName)
}
