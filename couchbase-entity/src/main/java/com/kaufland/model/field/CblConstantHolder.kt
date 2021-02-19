package com.kaufland.model.field

import com.kaufland.generation.TypeConversionMethodsGeneration
import com.kaufland.javaToKotlinType
import com.kaufland.model.deprecated.DeprecatedModel
import com.kaufland.util.ConversionUtil
import com.kaufland.util.TypeUtil
import com.squareup.kotlinpoet.*


import java.util.Arrays

import kaufland.com.coachbasebinderapi.Field

/**
 * Created by sbra0902 on 21.06.17.
 */

class CblConstantHolder(field: Field) : CblBaseFieldHolder(field.name, field) {

    val constantValue: String = field.defaultValue

    val constantValueAccessorName = "DOC_$constantName"


    override fun interfaceProperty(): PropertySpec {
        val returnType = TypeUtil.parseMetaType(typeMirror, isIterable, null)

        return PropertySpec.builder(accessorSuffix(), returnType, KModifier.PUBLIC).build()
    }

    override fun property(dbName: String?, possibleOverrides: Set<String>, useMDocChanges: Boolean, deprecated: DeprecatedModel?): PropertySpec {
        val returnType = TypeUtil.parseMetaType(typeMirror, isIterable, null)

        val builder = PropertySpec.builder(accessorSuffix(), returnType, KModifier.PUBLIC, KModifier.OVERRIDE)
                .getter(FunSpec.getterBuilder().addStatement("return " + TypeConversionMethodsGeneration.READ_METHOD_NAME + "(mDoc.get(%N), %T::class)!!", constantName, returnType).build())

        if (comment.isNotEmpty()) {
            builder.addKdoc(comment.joinToString(separator = "\n"))
        }

        return builder.build()
    }

    override fun createFieldConstant(): List<PropertySpec> {

        val fieldAccessorConstant = PropertySpec.builder(constantName, String::class, KModifier.FINAL, KModifier.PUBLIC).initializer("%S", dbField).addAnnotation(JvmField::class).build()

        return Arrays.asList(fieldAccessorConstant,
                PropertySpec.builder(constantValueAccessorName, typeMirror.asTypeName().javaToKotlinType(), KModifier.FINAL, KModifier.PUBLIC).initializer(ConversionUtil.convertStringToDesiredFormat(typeMirror, constantValue)).addAnnotation(JvmField::class).build())
    }

    override fun builderSetter(dbName: String?, packageName: String, entitySimpleName: String, useMDocChanges: Boolean): FunSpec? {
        return null
    }
}
