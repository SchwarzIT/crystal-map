package com.schwarz.crystalcore.generation.model

import com.schwarz.crystalapi.util.CrystalWrap
import com.schwarz.crystalcore.model.entity.BaseEntityHolder
import com.schwarz.crystalcore.model.typeconverter.TypeConverterHolderForEntityGeneration
import com.schwarz.crystalcore.util.TypeUtil
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeName

private const val RESULT_VAL_NAME = "result"

object EnsureTypesGeneration {
    fun <T> ensureTypes(
        holder: BaseEntityHolder<T>,
        useNullableMap: Boolean,
        typeConvertersByConvertedClass: Map<TypeName, TypeConverterHolderForEntityGeneration>
    ): FunSpec {
        val explicitType =
            if (useNullableMap) TypeUtil.hashMapStringAnyNullable() else TypeUtil.hashMapStringAny()
        val type = if (useNullableMap) TypeUtil.mapStringAnyNullable() else TypeUtil.mapStringAny()
        val ensureTypes = FunSpec.builder("ensureTypes").addParameter("doc", type).returns(type)
        ensureTypes.addStatement("val %N = %T()", RESULT_VAL_NAME, explicitType)
        ensureTypes.addStatement("%N.putAll(doc)", RESULT_VAL_NAME)

        for (field in holder.fields.values) {
            if (!field.isNonConvertibleClass && !field.isTypeOfSubEntity) {
                val typeConverterHolder =
                    typeConvertersByConvertedClass.get(field.fieldType)!!
                if (field.isIterable) {
                    ensureTypes.addStatement(
                        "%T.ensureListType(%N, %N, %T)",
                        CrystalWrap::class,
                        RESULT_VAL_NAME,
                        field.constantName,
                        typeConverterHolder.instanceClassTypeName
                    )
                } else {
                    ensureTypes.addStatement(
                        "%T.ensureType(%N, %N, %T)",
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
