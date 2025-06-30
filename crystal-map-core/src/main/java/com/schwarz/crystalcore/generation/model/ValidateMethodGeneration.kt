package com.schwarz.crystalcore.generation.model

import com.schwarz.crystalapi.util.CrystalWrap
import com.schwarz.crystalcore.model.entity.BaseEntityHolder
import com.schwarz.crystalcore.util.TypeUtil
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier

object ValidateMethodGeneration {

    fun <T>generate(holder: BaseEntityHolder<T>, useMDocChanges: Boolean): FunSpec {
        val validateBuilder =
            FunSpec.builder("validate").addModifiers(KModifier.PUBLIC, KModifier.OVERRIDE)
        val mandatoryFields = holder.fields.values.filter { it.mandatory }.map { it.constantName }

        if (mandatoryFields.isNotEmpty()) {
            val statement =
                "%T.validate(toMap(), %M(${mandatoryFields.map { "%N" }.joinToString()}))"
            val arguments = mutableListOf(CrystalWrap::class, TypeUtil.arrayOf())
            for (mandatoryField in mandatoryFields) {
                arguments.add(mandatoryField)
            }

            validateBuilder.addStatement(
                statement,
                *arguments.toTypedArray()
            )
        }

        return validateBuilder.build()
    }
}
