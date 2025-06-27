package com.schwarz.crystalcore.generation.model

import com.schwarz.crystalcore.model.entity.BaseEntityHolder
import com.schwarz.crystalcore.util.TypeUtil
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier

object CblConstantGeneration {

    fun <T>addConstants(holder: BaseEntityHolder<T>, useNullableMap: Boolean): FunSpec {
        val type = if (useNullableMap) TypeUtil.mutableMapStringAnyNullable() else TypeUtil.mutableMapStringAny()
        val builder = FunSpec.builder("addConstants").addModifiers(KModifier.PRIVATE).addParameter("map", type)

        for (fieldHolder in holder.fieldConstants.values) {
            if (fieldHolder.isConstant) {
                builder.addStatement("map.put(%N, DOC_%N)", fieldHolder.constantName, fieldHolder.constantName)
            }
        }
        return builder.build()
    }

    fun addAddCall(nameOfMap: String): CodeBlock {
        return CodeBlock.builder().addStatement("addConstants(%N)", nameOfMap).build()
    }
}
