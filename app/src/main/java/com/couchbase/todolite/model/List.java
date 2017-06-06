package com.couchbase.todolite.model;

import java.util.ArrayList;

import kaufland.com.coachbasebinderapi.CblEntity;
import kaufland.com.coachbasebinderapi.CblField;

@CblEntity
public class List {

    @CblField
    private String type;

    @CblField("title")
    private String title;

    @CblField("created_at")
    private String createdAt;

    @CblField("members")
    private ArrayList<String> members;

    @CblField("owner")
    private String owner;

    @CblField("sub")
    private Sub sub;

    @CblField("list_sub")
    private ArrayList<Sub> listSub;
}
