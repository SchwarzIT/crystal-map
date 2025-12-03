package com.schwarz.crystalcore.generation.model

import com.schwarz.crystalcore.model.entity.BaseEntityHolder
import com.schwarz.crystalcore.util.TypeUtil
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier

object MapSupportGeneration {
    fun <T> toMap(holder: BaseEntityHolder<T>): FunSpec {
        val toMapBuilder =
            FunSpec.builder("toMap").addModifiers(KModifier.OVERRIDE).returns(
                TypeUtil.mutableMapStringAny()
            ).addStatement("return toMap(this)")

        return toMapBuilder.build()
    }
}
