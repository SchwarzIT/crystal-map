package com.kaufland.model.entity;

import com.kaufland.model.field.CblBaseFieldHolder;
import com.kaufland.model.field.CblConstantHolder;
import com.kaufland.model.field.CblFieldHolder;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import com.sun.tools.javac.code.Symbol;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Element;

public abstract class CblBaseEntityHolder {

    private List<CblFieldHolder> mFields = new ArrayList<>();

    private List<CblConstantHolder> mFieldConstants = new ArrayList<>();

    private Element sourceElement;


    public List<CblFieldHolder> getFields() {
        return mFields;
    }

    public List<CblBaseFieldHolder> getAllFields() {
        List<CblBaseFieldHolder> allField = new ArrayList<>();
        allField.addAll(getFields());
        allField.addAll(getFieldConstants());
        return allField;
    }

    public List<CblConstantHolder> getFieldConstants() {
        return mFieldConstants;
    }

    public String getSourceClazzSimpleName() {
        return ((Symbol.ClassSymbol) getSourceElement()).getSimpleName().toString();
    }

    public String getEntitySimpleName() {
        return getSourceClazzSimpleName() + "Entity";
    }

    public String getPackage() {
        return ((Symbol.ClassSymbol) getSourceElement()).packge().toString();
    }

    public TypeName getEntityTypeName() {
        return ClassName.get(getPackage(), getEntitySimpleName());
    }

    public Element getSourceElement() {
        return sourceElement;
    }

    public void setSourceElement(Element sourceElement) {
        this.sourceElement = sourceElement;
    }
}
