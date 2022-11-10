package com.schwarz.crystalapi.query;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.schwarz.crystalapi.Field;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE})
public @interface Queries {

    Query[] value() default {};
}
