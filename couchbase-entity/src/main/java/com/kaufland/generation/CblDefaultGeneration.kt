package com.kaufland.generation

import com.kaufland.javaToKotlinType
import com.kaufland.model.entity.BaseEntityHolder
import com.kaufland.util.TypeUtil
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.asTypeName
import javax.lang.model.type.TypeMirror

object CblDefaultGeneration {

    fun addDefaults(holder: BaseEntityHolder): FunSpec {
        val builder = FunSpec.builder("addDefaults").addModifiers(KModifier.PRIVATE).addParameter( "map", TypeUtil.mutableMapStringAnyNullable())

        for (fieldHolder in holder.fields.values) {

            if (fieldHolder.isDefault) {
                builder.addStatement("map.put(%N, " + getConvertedValue(fieldHolder.typeMirror, fieldHolder.defaultValue) + ")", fieldHolder.constantName)
            }
        }
        return builder.build()
    }

    fun addAddCall(nameOfMap: String): CodeBlock {
        return CodeBlock.builder().addStatement("addDefaults(%N)", nameOfMap).build()
    }

    private fun getConvertedValue(clazz: TypeMirror, value: String): String {

        return if (clazz.asTypeName().javaToKotlinType() == TypeUtil.string()) {
            "\"" + value + "\""
        } else value
    }

}
