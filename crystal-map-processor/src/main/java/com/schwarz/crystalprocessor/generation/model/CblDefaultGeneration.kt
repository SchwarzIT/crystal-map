package com.schwarz.crystalprocessor.generation.model

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
            FunSpec.builder("addDefaults").addModifiers(KModifier.PRIVATE)

        for (fieldHolder in holder.fields.values) {
            if (fieldHolder.isDefault) {
                builder.addStatement(
                    "this.%N = ${ConversionUtil.convertStringToDesiredFormat(
                        fieldHolder.typeMirror,
                        fieldHolder.defaultValue
                    )}",
                    fieldHolder.dbField
                )
            }
        }
        return builder.build()
    }

    fun addAddCall(): CodeBlock {
        return CodeBlock.builder().addStatement("addDefaults()").build()
    }
}
