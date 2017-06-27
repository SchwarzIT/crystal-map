package com.couchbase.todolite.model;

import java.io.InputStream;

import kaufland.com.coachbasebinderapi.CblEntity;
import kaufland.com.coachbasebinderapi.CblField;

@CblEntity
public class Task {


    @CblField("type")
    private String type;

    @CblField("title")
    private String title;

    @CblField("checked")
    private boolean checked;

    @CblField("created_at")
    private String createdAt;

    @CblField("updated_at")
    private String updatedAt;

    @CblField("list_id")
    private String listId;

    @CblField(value = "image", attachmentType = "image/jpg")
    private InputStream image;


}
