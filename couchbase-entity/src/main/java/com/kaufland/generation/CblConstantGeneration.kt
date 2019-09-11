package com.kaufland.generation

import com.kaufland.model.entity.BaseEntityHolder
import com.kaufland.util.TypeUtil
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.asTypeName
import javax.lang.model.type.TypeMirror

object CblConstantGeneration {

    fun addConstants(holder: BaseEntityHolder): FunSpec {
        val builder = FunSpec.builder("addConstants").addModifiers(KModifier.PRIVATE).addParameter("map", TypeUtil.mutableMapStringObject())

        for (fieldHolder in holder.fieldConstants) {

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
