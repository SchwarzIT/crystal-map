package com.kaufland.model.source;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import com.sun.tools.javac.code.Symbol;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Element;

public class CblEntityHolder {

    private List<CblFieldHolder> mFields = new ArrayList<>();

    private List<CblConstantHolder> mFieldConstants = new ArrayList<>();

    private List<CblAttachmentFieldHolder> mFieldAttachments = new ArrayList<>();

    private Element sourceElement;

    private String dbName;

    private String id;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public List<CblFieldHolder> getFields() {
        return mFields;
    }

    public List<CblBaseFieldHolder> getAllFields() {
        List<CblBaseFieldHolder> allField = new ArrayList<>();
        allField.addAll(getFields());
        allField.addAll(getFieldAttachments());
        allField.addAll(getFieldConstants());
        return allField;
    }

    public List<CblConstantHolder> getFieldConstants() {
        return mFieldConstants;
    }

    public List<CblAttachmentFieldHolder> getFieldAttachments() {
        return mFieldAttachments;
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
