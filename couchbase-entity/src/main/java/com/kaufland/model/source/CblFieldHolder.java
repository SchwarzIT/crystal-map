package com.kaufland.model.source;

import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.JDirectClass;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;

/**
 * Created by sbra0902 on 02.06.17.
 */

public class CblFieldHolder {

    public String dbField;

    public String clazzFieldName;

    public AbstractJClass type;

    public String attachmentType;

    public String subEntityName;

    public boolean subEntityIsTypeParam;

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

    public boolean isTypeOfSubEntity(){
        return !StringUtils.isBlank(subEntityName);
    }

    public boolean isAttachment(){
        return !StringUtils.isBlank(attachmentType);
    }

    public AbstractJClass getType() {
        return type;
    }
}
