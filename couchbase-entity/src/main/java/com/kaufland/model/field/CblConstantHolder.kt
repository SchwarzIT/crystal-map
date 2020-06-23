package com.kaufland.model.field

import com.kaufland.generation.TypeConversionMethodsGeneration
import com.kaufland.util.TypeUtil
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier


import java.util.Arrays

import kaufland.com.coachbasebinderapi.Field

/**
 * Created by sbra0902 on 21.06.17.
 */

class CblConstantHolder(field: Field) : CblBaseFieldHolder(field.name, field) {

    val constantValue: String = field.defaultValue

    val constantValueAccessorName = "DOC_$constantName"

    override fun property(dbName: String?, possibleOverrides: Set<String>, useMDocChanges: Boolean): PropertySpec {
        val returnType = TypeUtil.parseMetaType(typeMirror!!, isIterable, null)

        val builder = PropertySpec.builder(accessorSuffix(), returnType, KModifier.PUBLIC).getter(FunSpec.getterBuilder().addStatement("return " + TypeConversionMethodsGeneration.READ_METHOD_NAME + "(mDoc.get(%N), %T::class)!!", constantName, returnType).build())
        return builder.build()
    }

    override fun createFieldConstant(): List<PropertySpec> {

        val fieldAccessorConstant = PropertySpec.builder(constantName, String::class, KModifier.FINAL, KModifier.PUBLIC).initializer("%S", dbField).addAnnotation(JvmField::class).build()

        return Arrays.asList(fieldAccessorConstant,
                PropertySpec.builder(constantValueAccessorName, String::class, KModifier.FINAL, KModifier.PUBLIC).initializer("%S", constantValue).addAnnotation(JvmField::class).build())
    }

    override fun builderSetter(dbName: String?, packageName: String, entitySimpleName: String, useMDocChanges: Boolean): FunSpec? {
        return null
    }
}
