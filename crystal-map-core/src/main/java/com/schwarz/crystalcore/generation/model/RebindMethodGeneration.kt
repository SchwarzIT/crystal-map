package com.schwarz.crystalcore.generation.model

import com.schwarz.crystalcore.util.TypeUtil
import com.squareup.kotlinpoet.FunSpec

class RebindMethodGeneration {
    fun generate(clearMDocChanges: Boolean, hasCacheableFields: Boolean = false): FunSpec {
        val explicitType =
            if (clearMDocChanges) {
                TypeUtil.hashMapStringAny()
            } else {
                TypeUtil
                    .linkedHashMapStringAnyNullable()
            }
        val type =
            if (clearMDocChanges) {
                TypeUtil.mapStringAny()
            } else {
                TypeUtil
                    .mapStringAnyNullable()
            }
        val rebind =
            FunSpec
                .builder("rebind")
                .addParameter("doc", type)
                .addStatement("mDoc = %T()", explicitType)
                .addCode(CblDefaultGeneration.addAddCall("mDoc"))
                .addStatement("mDoc.putAll(doc)")
                .addCode(CblConstantGeneration.addAddCall("mDoc"))

        if (clearMDocChanges) {
            rebind.addStatement("mDocChanges = %T()", TypeUtil.hashMapStringAnyNullable())
        }

        if (hasCacheableFields) {
            rebind.addStatement("_cacheGen++")
        }

        return rebind.build()
    }
}
