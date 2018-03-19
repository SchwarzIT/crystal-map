package com.kaufland.model.source;

import com.helger.jcodemodel.AbstractJClass;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Element;

public class CblEntityHolder {

    private List<CblBaseFieldHolder> mFields = new ArrayList<>();

    private AbstractJClass sourceClazz;

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

    public List<CblBaseFieldHolder> getFields() {
        return mFields;
    }

    public AbstractJClass getSourceClazz() {
        return sourceClazz;
    }

    public void setSourceClazz(AbstractJClass sourceClazz) {
        this.sourceClazz = sourceClazz;
    }

    public Element getSourceElement() {
        return sourceElement;
    }

    public void setSourceElement(Element sourceElement) {
        this.sourceElement = sourceElement;
    }
}
