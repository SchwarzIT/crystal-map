package kaufland.com.demo.entity;

import kaufland.com.coachbasebinderapi.CblDefault;
import kaufland.com.coachbasebinderapi.CblEntity;
import kaufland.com.coachbasebinderapi.CblField;

@CblEntity
public class UserComment {

    @CblField(value = "comment")
    private String comment;

    @CblField("user")
    @CblDefault("anonymous")
    private String userName;
}