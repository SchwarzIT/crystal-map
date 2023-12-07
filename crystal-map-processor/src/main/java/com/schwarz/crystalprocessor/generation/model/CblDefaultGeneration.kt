package com.schwarz.crystalprocessor.generation.model

import com.schwarz.crystalapi.util.CrystalWrap
import com.schwarz.crystalprocessor.model.entity.BaseEntityHolder
import com.schwarz.crystalprocessor.util.ConversionUtil
import com.schwarz.crystalprocessor.util.TypeUtil
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier

object CblDefaultGeneration {

    fun addDefaults(holder: BaseEntityHolder, useNullableMap: Boolean): FunSpec {
        val type =
            if (useNullableMap) TypeUtil.mutableMapStringAnyNullable() else TypeUtil.mutableMapStringAny()
        val valueType =
            if (useNullableMap) TypeUtil.anyNullable() else TypeUtil.any()

        val typeConversionReturnType =
            if (useNullableMap) TypeUtil.anyNullable() else TypeUtil.any()

        val builder =
            FunSpec.builder("addDefaults").addModifiers(KModifier.PRIVATE).addParameter("map", type)

        builder.addStatement("%T.addDefaults<%T, %T>(listOf(", CrystalWrap::class, typeConversionReturnType, valueType)
        for (fieldHolder in holder.fields.values) {
            if (fieldHolder.isDefault) {
                builder.addStatement(
                    "arrayOf(%N, %T::class, ${ConversionUtil.convertStringToDesiredFormat(
                        fieldHolder.typeMirror,
                        fieldHolder.defaultValue
                    )}),",
                    fieldHolder.constantName,
                    fieldHolder.fieldType
                )
            }
        }
        builder.addStatement("), map)")
        return builder.build()
    }

    fun addAddCall(nameOfMap: String): CodeBlock {
        return CodeBlock.builder().addStatement("addDefaults(%N)", nameOfMap).build()
    }
}
