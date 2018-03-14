package com.couchbase.todolite.model;

import kaufland.com.coachbasebinderapi.CblEntity;
import kaufland.com.coachbasebinderapi.CblField;

@CblEntity(database = "other")
public class OtherDatabase {

    @CblField("foo")
    private String bar;

}
