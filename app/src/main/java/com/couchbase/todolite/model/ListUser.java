package com.couchbase.todolite.model;

import kaufland.com.coachbasebinderapi.CblDefault;
import kaufland.com.coachbasebinderapi.CblEntity;
import kaufland.com.coachbasebinderapi.CblField;

@CblEntity
public class ListUser {

    @CblField(value = "display")
    private String displayName;

    @CblField("user")
    private String userName;
}