package com.kaufland.generation;

import com.kaufland.model.entity.BaseEntityHolder;
import com.kaufland.model.field.CblFieldHolder;
import com.kaufland.util.TypeUtil;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.thoughtworks.qdox.model.JavaField;

import javax.lang.model.element.Modifier;

public class CblDefaultGeneration {

    public static FieldSpec field() {
        return FieldSpec.builder(TypeUtil.createMapStringObject(), "mDocDefaults", Modifier.PRIVATE, Modifier.STATIC).
                initializer(" new $T()", TypeUtil.createHashMapStringObject()).
                build();
    }

    public static CodeBlock staticInitialiser(BaseEntityHolder holder) {
        CodeBlock.Builder builder = CodeBlock.builder();
        for (CblFieldHolder fieldHolder : holder.getFields()) {

            if (fieldHolder.getDefaultHolder() != null) {
                builder.addStatement("mDocDefaults.put($N, " + getConvertedValue(fieldHolder.getMetaField(), fieldHolder.getDefaultHolder().getDefaultValue())+")", fieldHolder.getConstantName());
            }
        }
        return builder.build();
    }

    private static String getConvertedValue(JavaField mMetaField, String value) {

        if (mMetaField.getType().getCanonicalName().equals(String.class.getCanonicalName())) {
            return "\"" + value + "\"";
        }
        return value;
    }

}
