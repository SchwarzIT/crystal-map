package com.kaufland.generation;

import com.kaufland.model.field.CblFieldHolder;
import com.kaufland.util.TypeUtil;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;

import java.util.List;

public class RebindMethodGeneration {

    public MethodSpec generate(List<CblFieldHolder> fieldHolders, boolean clearMDocChanges) {

        MethodSpec.Builder rebind = MethodSpec.methodBuilder("rebind").
                addParameter(TypeUtil.createMapStringObject(), "doc").
                addStatement("mDoc = doc != null ? doc : new java.util.HashMap<String, Object>()");

        if (clearMDocChanges) {
            rebind.addStatement("mDocChanges = new java.util.HashMap<String, Object>()");
        }

        rebind.addStatement("$T mDocDefaults = new $T()", TypeUtil.createHashMapStringObject(), TypeUtil.createHashMapStringObject());

        for (CblFieldHolder fieldHolder : fieldHolders) {
            if (fieldHolder.getDefaultHolder() != null) {

                rebind.addCode(CodeBlock.builder().
                        beginControlFlow("if(!mDoc.containsKey($N))", fieldHolder.getConstantName()).
                        addStatement("mDocDefaults.put($N, $N)", fieldHolder.getConstantName(), convertDefaultValue(fieldHolder)).
                        endControlFlow().
                        build());
            }
        }

        rebind.addCode(CodeBlock.builder().
                beginControlFlow("if(mDocDefaults.size()>0)").
                addStatement("mDoc.putAll(mDocDefaults)").
                endControlFlow().
                build());

        return rebind.build();

    }

    private String convertDefaultValue(CblFieldHolder fieldHolder) {

        if (fieldHolder.getMetaField().getType().getCanonicalName().equals(String.class.getCanonicalName())) {
            return "\"" + fieldHolder.getDefaultHolder().getDefaultValue() + "\"";
        }
        return fieldHolder.getDefaultHolder().getDefaultValue();
    }
}
