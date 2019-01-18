package com.kaufland.model.entity;

public class EntityHolder extends BaseEntityHolder {

    private String dbName;

    public EntityHolder(String dbName) {
        this.dbName = dbName;
    }

    public String getDbName() {
        return dbName;
    }

}
