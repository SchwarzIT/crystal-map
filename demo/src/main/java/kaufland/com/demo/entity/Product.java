package kaufland.com.demo.entity;

import com.couchbase.lite.Blob;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import kaufland.com.coachbasebinderapi.CblConstant;
import kaufland.com.coachbasebinderapi.CblDefault;
import kaufland.com.coachbasebinderapi.CblEntity;
import kaufland.com.coachbasebinderapi.CblField;
import kaufland.com.demo.Application;

@CblEntity(database = Application.DB)
public class Product {

    @CblConstant(value = "type", constant = "product")
    private String type;

    @CblField("name")
    @CblDefault("unknown")
    private String name;

    @CblField("comments")
    private List<UserComment> comments;

    @CblField(value = "image")
    private Blob image;

}
