package com.couchbase.todolite.model;

import android.graphics.Bitmap;

import com.couchbase.lite.Document;
import com.couchbase.lite.UnsavedRevision;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import kaufland.com.coachbasebinderapi.CblEntity;
import kaufland.com.coachbasebinderapi.CblField;

/**
 * Created by sbra0902 on 30.05.17.
 */
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

    @CblField("list_id")
    private String listId;

    @CblField(value = "image", attachmentType = "image/jpg")
    private InputStream image;


}
