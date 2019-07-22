package com.kaufland.generation;

import com.kaufland.model.field.CblFieldHolder;
import com.kaufland.util.TypeUtil;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;

import java.util.List;

public class RebindMethodGeneration {

    public MethodSpec generate(boolean clearMDocChanges) {

        MethodSpec.Builder rebind = MethodSpec.methodBuilder("rebind").
                addParameter(TypeUtil.createMapStringObject(), "doc").
                addStatement("mDoc = new $T()", TypeUtil.createHashMapStringObject()).
                addCode(CblDefaultGeneration.addAddCall("mDoc")).
                addCode(CodeBlock.builder()
                        .beginControlFlow("if(doc != null)")
                        .addStatement("mDoc.putAll(doc)")
                        .endControlFlow().build()).
                addCode(CblConstantGeneration.addAddCall("mDoc"));

        if (clearMDocChanges) {
            rebind.addStatement("mDocChanges = new $T()", TypeUtil.createHashMapStringObject());
        }

        return rebind.build();

    }
}
