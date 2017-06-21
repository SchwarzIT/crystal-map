package com.kaufland.model.source;

import com.helger.jcodemodel.AbstractJClass;

import javax.lang.model.element.Element;

/**
 * Created by sbra0902 on 21.06.17.
 */

public class CblBaseFieldHolder {

    private String dbField;

    private String clazzFieldName;

    private AbstractJClass type;

    private Element fieldElement;


    public String getDbField() {
        return dbField;
    }

    public void setDbField(String dbField) {
        this.dbField = dbField;
    }

    public String getClazzFieldName() {
        return clazzFieldName;
    }

    public void setClazzFieldName(String clazzFieldName) {
        this.clazzFieldName = clazzFieldName;
    }

    public AbstractJClass getType() {
        return type;
    }

    public void setType(AbstractJClass type) {
        this.type = type;
    }

    public Element getFieldElement() {
        return fieldElement;
    }

    public void setFieldElement(Element fieldElement) {
        this.fieldElement = fieldElement;
    }
}
