package com.kaufland.model.field

import com.kaufland.generation.TypeConversionMethodsGeneration
import com.kaufland.util.TypeUtil
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeName


import java.util.Arrays

import javax.lang.model.element.Modifier

import kaufland.com.coachbasebinderapi.Field

/**
 * Created by sbra0902 on 21.06.17.
 */

class CblConstantHolder(field: Field) : CblBaseFieldHolder(field.name, field) {

    val constantValue: String

    init {
        constantValue = field.defaultValue
    }

    override fun getter(dbName: String?, useMDocChanges: Boolean): FunSpec {
        val returnType = TypeUtil.parseMetaType(typeMirror!!, isIterable, null)

        val builder = FunSpec.builder("get" + accessorSuffix()).addModifiers(KModifier.PUBLIC).returns(returnType).addStatement("return " + TypeConversionMethodsGeneration.READ_METHOD_NAME + "(mDoc.get(\$N), \$T.class)", constantName, returnType)
        return builder.build()
    }

    override fun setter(dbName: String?, entityTypeName: TypeName, useMDocChanges: Boolean): FunSpec? {
        return null
    }

    override fun createFieldConstant(): List<PropertySpec> {

        val fieldAccessorConstant = PropertySpec.builder(constantName, String::class.java, KModifier.FINAL, KModifier.PUBLIC).initializer("\$S", dbField).build()

        return Arrays.asList(fieldAccessorConstant,
                PropertySpec.builder("DOC_$constantName", String::class.java, KModifier.FINAL, KModifier.PUBLIC).initializer("\$S", constantValue).build())
    }
}
