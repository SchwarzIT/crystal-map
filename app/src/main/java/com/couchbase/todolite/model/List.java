package com.couchbase.todolite.model;

import java.util.ArrayList;

import kaufland.com.coachbasebinderapi.CblEntity;
import kaufland.com.coachbasebinderapi.CblField;

/**
 * Created by sbra0902 on 24.05.17.
 */

@CblEntity
public class List {

    @CblField
    String type;

    @CblField("title")
    String title;

    @CblField("created_at")
    String createdAt;

    @CblField("members")
    ArrayList<String> members;

    @CblField("owner")
    String owner;

}
