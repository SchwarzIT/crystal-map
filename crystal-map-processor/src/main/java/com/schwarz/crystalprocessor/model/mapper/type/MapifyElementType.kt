package com.schwarz.crystalprocessor.model.mapper.type

import com.schwarz.crystalprocessor.ProcessingContext
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import javax.lang.model.element.Element

interface MapifyElementType {

    val elements: Array<Element>

    val fieldName: String

    val mapName: String

    val typeName: TypeName

    val accessible: Boolean

    val declaringName: ProcessingContext.DeclaringName

    val reflectedFieldName: String
        get() = "a${fieldName.capitalize()}"

    val accessorName: String
        get() = "${fieldName}_"

    fun reflectionProperties(sourceClazzTypeName: TypeName): List<PropertySpec>

    fun getterFunSpec(): FunSpec

    fun setterFunSpec(): FunSpec
}
