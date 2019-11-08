package com.kaufland.generation

import com.kaufland.model.field.CblFieldHolder
import com.kaufland.util.TypeUtil
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec

class RebindMethodGeneration {

    fun generate(clearMDocChanges: Boolean): FunSpec {

        val rebind = FunSpec.builder("rebind").addParameter( "doc", TypeUtil.mapStringObject()).addStatement("mDoc = %T()", TypeUtil.hashMapStringObject()).addCode(CblDefaultGeneration.addAddCall("mDoc")).addCode(CodeBlock.builder()
                .beginControlFlow("if(doc != null)")
                .addStatement("mDoc.putAll(doc)")
                .endControlFlow().build()).addCode(CblConstantGeneration.addAddCall("mDoc"))

        if (clearMDocChanges) {
            rebind.addStatement("mDocChanges = %T()", TypeUtil.hashMapStringObject())
        }

        return rebind.build()

    }
}
