package kaufland.com.demo.entity;

import java.io.InputStream;
import java.util.ArrayList;

import kaufland.com.coachbasebinderapi.CblConstant;
import kaufland.com.coachbasebinderapi.CblEntity;
import kaufland.com.coachbasebinderapi.CblField;
import kaufland.com.demo.Application;

@CblEntity(database = Application.DB)
public class Product {

    @CblConstant(value = "type", constant = "product")
    private String type;

    @CblField("name")
    private String name;

    @CblField("comments")
    private ArrayList<UserComment> comments;

    @CblField(value = "image", attachmentType = "image/jpg")
    private InputStream inputStream;
}
