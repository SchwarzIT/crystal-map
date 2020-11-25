package com.kaufland.generation

import com.kaufland.javaToKotlinType
import com.kaufland.model.entity.BaseEntityHolder
import com.kaufland.util.ConversionUtil
import com.kaufland.util.TypeUtil
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.asTypeName
import javax.lang.model.type.TypeMirror

object CblDefaultGeneration {

    fun addDefaults(holder: BaseEntityHolder, useNullableMap : Boolean): FunSpec {

        val type = if (useNullableMap) TypeUtil.mutableMapStringAnyNullable() else TypeUtil.mutableMapStringAny()
        val typeConversionReturnType = if (useNullableMap) TypeUtil.anyNullable() else TypeUtil.any()

        val builder = FunSpec.builder("addDefaults").addModifiers(KModifier.PRIVATE).addParameter( "map", type)

        for (fieldHolder in holder.fields.values) {

            if (fieldHolder.isDefault) {
                builder.addStatement("map.put(%N, " +  fieldHolder.ensureType(typeConversionReturnType, ConversionUtil.convertStringToDesiredFormat(fieldHolder.typeMirror, fieldHolder.defaultValue)) + "!!)", fieldHolder.constantName)
            }
        }
        return builder.build()
    }

    fun addAddCall(nameOfMap: String): CodeBlock {
        return CodeBlock.builder().addStatement("addDefaults(%N)", nameOfMap).build()
    }

}
