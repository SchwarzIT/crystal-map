package com.kaufland.generation;

import com.kaufland.model.entity.BaseEntityHolder;
import com.kaufland.util.TypeUtil;
import com.squareup.javapoet.MethodSpec;

import javax.lang.model.element.Modifier;

public class MapSupportGeneration {

    public static MethodSpec toMap(BaseEntityHolder holder) {
        MethodSpec.Builder toMapBuilder = MethodSpec.methodBuilder("toMap").addModifiers(Modifier.PUBLIC).
                returns(TypeUtil.createMapStringObject()).
                addStatement("return toMap(this)");

        return toMapBuilder.build();
    }

}
