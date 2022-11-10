package com.schwarz.crystalprocessor.generation.model

import com.schwarz.crystalprocessor.model.entity.BaseEntityHolder
import com.schwarz.crystalprocessor.util.TypeUtil
import com.squareup.kotlinpoet.FunSpec

object EnsureTypesGeneration {

    fun ensureTypes(holder: BaseEntityHolder, useNullableMap: Boolean): FunSpec {

        val explicitType =
            if (useNullableMap) TypeUtil.hashMapStringAnyNullable() else TypeUtil.hashMapStringAny()
        val type = if (useNullableMap) TypeUtil.mapStringAnyNullable() else TypeUtil.mapStringAny()
        val typeConversionReturnType =
            if (useNullableMap) TypeUtil.anyNullable() else TypeUtil.any()
        val ensureTypes = FunSpec.builder("ensureTypes").addParameter("doc", type).returns(type)
        ensureTypes.addStatement("val result = %T()", explicitType)
        ensureTypes.addStatement("result.putAll(doc)")

        for (field in holder.fields.values) {
            ensureTypes.beginControlFlow(
                "${
                field.ensureType(
                    typeConversionReturnType,
                    "doc[%N], %N",
                    field.constantName,
                    field.constantName
                )
                }?.let"
            )
            ensureTypes.addStatement("result[%N] = it", field.constantName)
            ensureTypes.endControlFlow()
        }

        ensureTypes.addStatement("return result")
        return ensureTypes.build()
    }
}
