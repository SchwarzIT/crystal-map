package com.kaufland.model.source;

import com.kaufland.util.ConversionUtil;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.thoughtworks.qdox.model.JavaField;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import javax.lang.model.element.Element;

/**
 * Created by sbra0902 on 21.06.17.
 */

public abstract class CblBaseFieldHolder {

    private String mDbField;

    private Element mFieldElement;

    private JavaField mMetaField;

    public CblBaseFieldHolder(String dbField, Element fieldElement, JavaField metaField) {
        mDbField = StringUtils.isEmpty(dbField) ? fieldElement.getSimpleName().toString() : dbField;
        mFieldElement = fieldElement;
        mMetaField = metaField;
    }


    public abstract MethodSpec getter(String dbName);

    public abstract MethodSpec setter(String dbName, TypeName entityTypeName);

    public abstract List<FieldSpec> createFieldConstant();

    public JavaField getMetaField() {
        return mMetaField;
    }

    public String getDbField() {
        return mDbField;
    }

    public String getConstantName(){
       return ConversionUtil.convertCamelToUnderscore(getDbField()).toUpperCase();
    }

    public Element getFieldElement() {
        return mFieldElement;
    }

}
