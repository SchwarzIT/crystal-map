package kaufland.com.demo.entity;

import kaufland.com.coachbasebinderapi.MapWrapper;
import kaufland.com.coachbasebinderapi.Default;
import kaufland.com.coachbasebinderapi.Field;

@MapWrapper
public class UserComment {

    @Field(value = "comment")
    private String comment;

    @Field("user")
    @Default("anonymous")
    private String userName;
}