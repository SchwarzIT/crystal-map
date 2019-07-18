package com.kaufland.model.field;

import com.kaufland.util.ConversionUtil;
import com.kaufland.util.FieldExtractionUtil;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.thoughtworks.qdox.model.JavaField;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

import kaufland.com.coachbasebinderapi.Field;

/**
 * Created by sbra0902 on 21.06.17.
 */

public abstract class CblBaseFieldHolder {

    private String mDbField;

    private Field mField;

    private TypeMirror mTypeMirror;

    public CblBaseFieldHolder(String dbField, Field field) {
        mDbField = dbField;
        mField = field;
        mTypeMirror = FieldExtractionUtil.typeMirror(field);
    }


    public String accessorSuffix(){
        return WordUtils.capitalize(mDbField.replaceAll("_", " ")).replaceAll(" ", "");
    }

    public boolean isIterable() {
        return mField.list();
    }

    public boolean isDefault() {
        return !mField.defaultValue().isEmpty() && !isConstant();
    }

    public boolean isConstant() {
        return mField.readonly();
    }

    public TypeMirror getTypeMirror() {
        return mTypeMirror;
    }

    public abstract MethodSpec getter(String dbName, boolean useMDocChanges);

    public abstract MethodSpec setter(String dbName, TypeName entityTypeName, boolean useMDocChanges);

    public abstract List<FieldSpec> createFieldConstant();

    public String getDbField() {
        return mDbField;
    }

    public String getConstantName(){
       return ConversionUtil.convertCamelToUnderscore(getDbField()).toUpperCase();
    }

    public String getDefaultValue() {
        return mField.defaultValue();
    }
}
