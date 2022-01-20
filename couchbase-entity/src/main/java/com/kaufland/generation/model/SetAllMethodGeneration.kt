package com.kaufland.generation.model

import com.kaufland.model.entity.BaseEntityHolder
import com.kaufland.util.TypeUtil
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier

class SetAllMethodGeneration {

    fun generate(holder: BaseEntityHolder, useMDocChanges: Boolean): FunSpec {

        val attributeName = if (useMDocChanges) "mDocChanges" else "mDoc"
        val setAllBuilder = FunSpec.builder("setAll").addModifiers(KModifier.PUBLIC, KModifier.OVERRIDE)
            .addParameter("map", TypeUtil.mapStringAnyNullable()).addStatement(
                "$attributeName.putAll(map)"
            )

        return setAllBuilder.build()
    }
}
