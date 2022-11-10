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
        val typeConversionReturnType =
            if (useNullableMap) TypeUtil.anyNullable() else TypeUtil.any()

        val builder =
            FunSpec.builder("addDefaults").addModifiers(KModifier.PRIVATE).addParameter("map", type)

        for (fieldHolder in holder.fields.values) {

            if (fieldHolder.isDefault) {
                builder.beginControlFlow("if(map[%N] == null)", fieldHolder.constantName)
                builder.addStatement(
                    "map.put(%N, " + fieldHolder.ensureType(
                        typeConversionReturnType,
                        ConversionUtil.convertStringToDesiredFormat(
                            fieldHolder.typeMirror,
                            fieldHolder.defaultValue
                        ) + ", %N",
                        fieldHolder.constantName
                    ) + "!!)",
                    fieldHolder.constantName
                )
                builder.endControlFlow()
            }
        }
        return builder.build()
    }

    fun addAddCall(nameOfMap: String): CodeBlock {
        return CodeBlock.builder().addStatement("addDefaults(%N)", nameOfMap).build()
    }
}
