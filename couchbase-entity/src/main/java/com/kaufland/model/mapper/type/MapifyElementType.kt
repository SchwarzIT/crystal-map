package com.kaufland.model.mapper.type

import com.kaufland.ProcessingContext
import com.kaufland.ProcessingContext.asDeclaringName
import com.kaufland.javaToKotlinType
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import javax.lang.model.element.Modifier

interface MapifyElementType {

    val fieldName : String

    val mapName : String

    val typeName : TypeName

    val accessible : Boolean

    val declaringName: ProcessingContext.DeclaringName

    val reflectedFieldName: String
        get() = "a${fieldName.capitalize()}"

    val accessorName: String
        get() = "${fieldName}_"


    fun reflectionProperties(sourceClazzTypeName: TypeName) : List<PropertySpec>

    fun getterFunSpec(): FunSpec

    fun setterFunSpec(): FunSpec
}