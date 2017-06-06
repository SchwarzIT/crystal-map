package com.kaufland.model.source;

import com.helger.jcodemodel.AbstractJClass;

import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.Element;

public class CblFieldHolder {

    private String dbField;

    private String clazzFieldName;

    private AbstractJClass type;

    private String attachmentType;

    private String subEntityName;

    private boolean subEntityIsTypeParam;

    private Element fieldElement;


    public void setType(AbstractJClass type) {
        this.type = type;
    }

    public String getAttachmentType() {
        return attachmentType;
    }

    public void setAttachmentType(String attachmentType) {
        this.attachmentType = attachmentType;
    }

    public String getSubEntityName() {
        return subEntityName;
    }

    public void setSubEntityName(String subEntityName) {
        this.subEntityName = subEntityName;
    }

    public boolean isSubEntityIsTypeParam() {
        return subEntityIsTypeParam;
    }

    public void setSubEntityIsTypeParam(boolean subEntityIsTypeParam) {
        this.subEntityIsTypeParam = subEntityIsTypeParam;
    }

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

    public boolean isTypeOfSubEntity() {
        return !StringUtils.isBlank(subEntityName);
    }

    public boolean isAttachment() {
        return !StringUtils.isBlank(attachmentType);
    }

    public AbstractJClass getType() {
        return type;
    }

    public Element getFieldElement() {
        return fieldElement;
    }

    public void setFieldElement(Element fieldElement) {
        this.fieldElement = fieldElement;
    }
}
