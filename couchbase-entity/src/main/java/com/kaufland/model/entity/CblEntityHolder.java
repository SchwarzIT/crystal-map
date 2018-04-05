package com.kaufland.model.entity;

public class CblEntityHolder extends CblBaseEntityHolder{

    private String dbName;

    public CblEntityHolder(String dbName) {
        this.dbName = dbName;
    }

    public String getDbName() {
        return dbName;
    }

}
