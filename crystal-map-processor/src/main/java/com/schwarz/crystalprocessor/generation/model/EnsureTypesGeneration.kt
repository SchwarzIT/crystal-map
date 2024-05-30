package com.schwarz.crystalprocessor.generation.model

import com.schwarz.crystalapi.util.CrystalWrap
import com.schwarz.crystalprocessor.model.entity.BaseEntityHolder
import com.schwarz.crystalprocessor.model.typeconverter.TypeConverterHolderForEntityGeneration
import com.schwarz.crystalprocessor.util.TypeUtil
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeName

private const val RESULT_VAL_NAME = "result"

object EnsureTypesGeneration {

    fun ensureTypes(
        holder: BaseEntityHolder,
        useNullableMap: Boolean,
        typeConvertersByConvertedClass: Map<TypeName, TypeConverterHolderForEntityGeneration>
    ): FunSpec {
        val explicitType =
            if (useNullableMap) TypeUtil.hashMapStringAnyNullable() else TypeUtil.hashMapStringAny()
        val type = if (useNullableMap) TypeUtil.mapStringAnyNullable() else TypeUtil.mapStringAny()
        val typeConversionReturnType =
            if (useNullableMap) TypeUtil.anyNullable() else TypeUtil.any()
        val ensureTypes = FunSpec.builder("ensureTypes").addParameter("doc", type).returns(type)
        ensureTypes.addStatement("val %N = %T()", RESULT_VAL_NAME, explicitType)
        ensureTypes.addStatement("%N.putAll(doc)", RESULT_VAL_NAME)

        for (field in holder.fields.values) {
            if (field.isNonConvertibleClass) {
                if (field.isIterable) {
                    ensureTypes.addStatement(
                        "%T.getList<%T>(mutableMapOf(), %N, %N)",
                        CrystalWrap::class,
                        field.fieldType,
                        RESULT_VAL_NAME,
                        field.constantName
                    )
                } else {
                    ensureTypes.addStatement(
                        "%T.get<%T>(mutableMapOf(), %N, %N)",
                        CrystalWrap::class,
                        field.fieldType,
                        RESULT_VAL_NAME,
                        field.constantName
                    )
                }
            } else if (field.isTypeOfSubEntity) {
                if (field.isIterable) {
                    ensureTypes.addStatement(
                        "%T.getList(mutableMapOf(), %N, %N, {%T.fromMap(it) ?: emptyList()})",
                        CrystalWrap::class,
                        RESULT_VAL_NAME,
                        field.constantName,
                        field.subEntityTypeName
                    )
                } else {
                    ensureTypes.addStatement(
                        "%T.get(mutableMapOf(), %N, %N, {%T.fromMap(it)})",
                        CrystalWrap::class,
                        RESULT_VAL_NAME,
                        field.constantName,
                        field.subEntityTypeName
                    )
                }
            } else {
                val typeConverterHolder =
                    typeConvertersByConvertedClass.get(field.fieldType)!!
                if (field.isIterable) {
                    ensureTypes.addStatement(
                        "%T.getList(mutableMapOf(), %N, %N, %T)",
                        CrystalWrap::class,
                        RESULT_VAL_NAME,
                        field.constantName,
                        typeConverterHolder.instanceClassTypeName
                    )
                } else {
                    ensureTypes.addStatement(
                        "%T.get(mutableMapOf(), %N, %N, %T)",
                        CrystalWrap::class,
                        RESULT_VAL_NAME,
                        field.constantName,
                        typeConverterHolder.instanceClassTypeName
                    )
                }
            }
        }

        ensureTypes.addStatement("return result")
        return ensureTypes.build()
    }
}
