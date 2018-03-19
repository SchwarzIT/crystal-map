package com.kaufland.model.source;

import org.apache.commons.lang3.StringUtils;

public class CblFieldHolder extends CblBaseFieldHolder{


    private String attachmentType;

    private String subEntityName;

    private boolean subEntityIsTypeParam;

    private CblDefaultHolder defaultHolder;

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

    public CblDefaultHolder getDefaultHolder() {
        return defaultHolder;
    }

    public void setDefaultHolder(CblDefaultHolder defaultHolder) {
        this.defaultHolder = defaultHolder;
    }

    public boolean isTypeOfSubEntity() {
        return !StringUtils.isBlank(subEntityName);
    }

    public boolean isAttachment() {
        return !StringUtils.isBlank(attachmentType);
    }

}
