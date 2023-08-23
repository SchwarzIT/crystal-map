package com.schwarz.crystalprocessor.generation.model

import com.schwarz.crystalapi.util.CrystalWrap
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

        ensureTypes.addStatement("result.putAll(%T.ensureTypes<%T>(mapOf(", CrystalWrap::class, typeConversionReturnType)
        for (field in holder.fields.values) {

            ensureTypes.addStatement("%N to %T::class,", field.constantName, field.evaluateClazzForTypeConversion())
        }
        ensureTypes.addStatement("), doc))")

        ensureTypes.addStatement("return result")
        return ensureTypes.build()
    }
}
