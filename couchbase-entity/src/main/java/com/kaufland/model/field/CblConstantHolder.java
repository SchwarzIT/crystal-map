package com.kaufland.model.field;

import com.kaufland.generation.TypeConversionMethodsGeneration;
import com.kaufland.util.TypeUtil;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.thoughtworks.qdox.model.JavaField;

import org.apache.commons.lang3.text.WordUtils;

import java.util.Arrays;
import java.util.List;

import javax.lang.model.element.Modifier;

import kaufland.com.coachbasebinderapi.Field;

/**
 * Created by sbra0902 on 21.06.17.
 */

public class CblConstantHolder extends CblBaseFieldHolder {

    private String mConstantValue;

    public CblConstantHolder(Field field) {
        super(field.name(), field);
        mConstantValue = field.defaultValue();
    }


    public String getConstantValue() {
        return mConstantValue;
    }

    @Override
    public MethodSpec getter(String dbName, boolean useMDocChanges) {
        TypeName returnType = TypeUtil.parseMetaType(getTypeMirror(), isIterable(), null);

        MethodSpec.Builder builder = MethodSpec.methodBuilder("get"  + accessorSuffix()).
                addModifiers(Modifier.PUBLIC).
                returns(returnType).
                addStatement("return " + TypeConversionMethodsGeneration.READ_METHOD_NAME + "(mDoc.get($N), $T.class)", getConstantName(), returnType);
        return builder.build();
    }

    @Override
    public MethodSpec setter(String dbName, TypeName entityTypeName, boolean useMDocChanges) {
        return null;
    }

    @Override
    public List<FieldSpec> createFieldConstant() {

        FieldSpec fieldAccessorConstant = FieldSpec.builder(String.class, getConstantName(), Modifier.FINAL, Modifier.PUBLIC, Modifier.STATIC).
                initializer("$S", getDbField()).
                build();

        return Arrays.asList(fieldAccessorConstant,
                FieldSpec.builder(String.class, "DOC_" + getConstantName(), Modifier.FINAL, Modifier.PUBLIC, Modifier.STATIC).
                        initializer("$S", getConstantValue()).
                        build());
    }
}
