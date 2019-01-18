package com.kaufland.model.entity;

public class WrapperEntityHolder extends BaseEntityHolder {

    @Override
    public String getEntitySimpleName() {
        return getSourceClazzSimpleName() + "Wrapper";
    }
}
