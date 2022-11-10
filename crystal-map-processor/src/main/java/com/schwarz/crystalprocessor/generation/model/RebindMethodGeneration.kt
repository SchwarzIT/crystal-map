package com.schwarz.crystalprocessor.generation.model

import com.schwarz.crystalprocessor.util.TypeUtil
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec

class RebindMethodGeneration {

    fun generate(clearMDocChanges: Boolean): FunSpec {

        val explicitType = if (clearMDocChanges) TypeUtil.hashMapStringAny() else TypeUtil.linkedHashMapStringAnyNullable()
        val type = if (clearMDocChanges) TypeUtil.mapStringAny() else TypeUtil.mapStringAnyNullable()
        val rebind = FunSpec.builder("rebind").addParameter("doc", type)
            .addStatement("mDoc = %T()", explicitType)
            .addCode(
                CodeBlock.builder()
                    .beginControlFlow("if(doc != null)")
                    .addStatement("mDoc.putAll(doc)")
                    .endControlFlow().build()
            )
            .addCode(CblDefaultGeneration.addAddCall("mDoc"))
            .addCode(CblConstantGeneration.addAddCall("mDoc"))

        if (clearMDocChanges) {
            rebind.addStatement("mDocChanges = %T()", TypeUtil.hashMapStringAnyNullable())
        }

        return rebind.build()
    }
}
