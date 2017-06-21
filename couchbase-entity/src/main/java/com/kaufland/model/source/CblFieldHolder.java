package com.kaufland.model.source;

import com.helger.jcodemodel.AbstractJClass;

import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.Element;

public class CblFieldHolder extends CblBaseFieldHolder{


    private String attachmentType;

    private String subEntityName;

    private boolean subEntityIsTypeParam;


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

    public boolean isTypeOfSubEntity() {
        return !StringUtils.isBlank(subEntityName);
    }

    public boolean isAttachment() {
        return !StringUtils.isBlank(attachmentType);
    }

}
