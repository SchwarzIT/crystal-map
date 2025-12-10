package com.schwarz.crystalcore.generation.model

import com.schwarz.crystalcore.model.entity.BaseEntityHolder
import com.schwarz.crystalcore.util.TypeUtil
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier

class SetAllMethodGeneration {
    fun <T> generate(
        holder: BaseEntityHolder<T>,
        useMDocChanges: Boolean
    ): FunSpec {
        val attributeName = if (useMDocChanges) "mDocChanges" else "mDoc"
        val setAllBuilder =
            FunSpec.builder("setAll").addModifiers(KModifier.PUBLIC, KModifier.OVERRIDE)
                .addParameter("map", TypeUtil.mapStringAnyNullable()).addStatement(
                    "$attributeName.putAll(map)"
                )

        return setAllBuilder.build()
    }
}
