package com.kaufland.generation.model

import com.kaufland.model.entity.BaseEntityHolder
import com.kaufland.util.TypeUtil
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier

object MapSupportGeneration {

    fun toMap(holder: BaseEntityHolder): FunSpec {
        val toMapBuilder = FunSpec.builder("toMap").addModifiers(KModifier.OVERRIDE).returns(TypeUtil.mutableMapStringAny()).addStatement("return toMap(this)")

        return toMapBuilder.build()
    }
}
