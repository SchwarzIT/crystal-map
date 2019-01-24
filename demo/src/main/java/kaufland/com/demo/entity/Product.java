package kaufland.com.demo.entity;

import com.couchbase.lite.Blob;

import java.util.List;

import kaufland.com.coachbasebinderapi.Constant;
import kaufland.com.coachbasebinderapi.Default;
import kaufland.com.coachbasebinderapi.Entity;
import kaufland.com.coachbasebinderapi.Field;
import kaufland.com.demo.Application;

@Entity(database = Application.DB)
public class Product {

    @Constant(value = "type", constant = "product")
    private String type;

    @Field("name")
    @Default("unknown")
    private String name;

    @Field("comments")
    private List<UserComment> comments;

    @Field(value = "image")
    private Blob image;

    @Field("identifiers")
    private List<String> identifiers;

}
