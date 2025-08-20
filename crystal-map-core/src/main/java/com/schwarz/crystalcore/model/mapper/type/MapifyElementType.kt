package com.schwarz.crystalcore.model.mapper.type

import com.schwarz.crystalcore.model.source.ISourceDeclaringName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName

interface MapifyElementType<T> {

    val elements: List<T>

    val fieldName: String

    val mapName: String

    val typeName: TypeName

    val accessible: Boolean

    val declaringName: ISourceDeclaringName

    val reflectedFieldName: String
        get() = "a${fieldName.capitalize()}"

    val accessorName: String
        get() = "${fieldName}_"

    fun reflectionProperties(sourceClazzTypeName: TypeName): List<PropertySpec>

    fun getterFunSpec(): FunSpec

    fun setterFunSpec(): FunSpec
}
