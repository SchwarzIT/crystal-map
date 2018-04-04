package com.kaufland.model.source;

import com.kaufland.util.TypeUtil;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.thoughtworks.qdox.model.JavaField;

import org.apache.commons.lang3.text.WordUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

import kaufland.com.coachbasebinderapi.CblConstant;

/**
 * Created by sbra0902 on 21.06.17.
 */

public class CblConstantHolder extends CblBaseFieldHolder {

    private String mConstantValue;

    public CblConstantHolder(CblConstant field, Element fieldElement, JavaField metaField) {
        super(field.value(), fieldElement, metaField);
        mConstantValue = field.constant();
    }


    public String getConstantValue() {
        return mConstantValue;
    }

    public void setConstantValue(String constantValue) {
        this.mConstantValue = constantValue;
    }

    @Override
    public MethodSpec getter(String dbName) {
        TypeName returnType = TypeUtil.parseMetaType(getMetaField().getType(), null);

        MethodSpec.Builder builder = MethodSpec.methodBuilder("get" + WordUtils.capitalize(getMetaField().getName())).
                addModifiers(Modifier.PUBLIC).
                returns(returnType).
                addStatement("return ($T) mDoc.get($N)", returnType, getConstantName());
        return builder.build();
    }

    @Override
    public MethodSpec setter(String dbName, TypeName entityTypeName) {
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
