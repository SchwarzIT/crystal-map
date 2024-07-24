package com.schwarz.crystalprocessor.generation.model

import com.schwarz.crystalprocessor.model.entity.BaseEntityHolder
import com.schwarz.crystalprocessor.model.typeconverter.TypeConverterHolderForEntityGeneration
import com.schwarz.crystalprocessor.util.ConversionUtil
import com.schwarz.crystalprocessor.util.TypeUtil
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeName

object CblDefaultGeneration {

    fun addDefaults(holder: BaseEntityHolder, useNullableMap: Boolean, typeConvertersByConvertedClass: Map<TypeName, TypeConverterHolderForEntityGeneration>): FunSpec {
        val type =
            if (useNullableMap) TypeUtil.mutableMapStringAnyNullable() else TypeUtil.mutableMapStringAny()
        val valueType =
            if (useNullableMap) TypeUtil.anyNullable() else TypeUtil.any()

        val typeConversionReturnType =
            if (useNullableMap) TypeUtil.anyNullable() else TypeUtil.any()

        val builder =
            FunSpec.builder("addDefaults").addModifiers(KModifier.PRIVATE).addParameter("map", type)

        builder.addStatement("val result = mutableMapOf<String, Any?>()")

        for (fieldHolder in holder.fields.values) {
            if (fieldHolder.isDefault) {
                fieldHolder.crystalWrapSetStatement(
                    builder,
                    "result",
                    typeConvertersByConvertedClass,
                    ConversionUtil.convertStringToDesiredFormat(
                        fieldHolder.typeMirror,
                        fieldHolder.defaultValue
                    )
                )
            }
        }

        builder.addCode(
            CodeBlock.builder()
                .beginControlFlow("result.forEach")
                .beginControlFlow("if(it.value != null)").addStatement("map[it.key] = it.value!!").endControlFlow()
                .endControlFlow()
                .build()
        )
        return builder.build()
    }

    fun addAddCall(nameOfMap: String): CodeBlock {
        return CodeBlock.builder().addStatement("addDefaults(%N)", nameOfMap).build()
    }
}
