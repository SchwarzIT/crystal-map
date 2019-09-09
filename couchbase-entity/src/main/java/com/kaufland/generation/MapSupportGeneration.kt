package com.kaufland.generation

import com.kaufland.model.entity.BaseEntityHolder
import com.kaufland.util.TypeUtil
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier

import javax.lang.model.element.Modifier

object MapSupportGeneration {

    fun toMap(holder: BaseEntityHolder): FunSpec {
        val toMapBuilder = FunSpec.builder("toMap").addModifiers(KModifier.PUBLIC).returns(TypeUtil.mapStringObject()).addStatement("return toMap(this)")

        return toMapBuilder.build()
    }

}
