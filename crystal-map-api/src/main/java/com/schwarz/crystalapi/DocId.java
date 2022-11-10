package com.schwarz.crystalapi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE})
public @interface DocId {

    String value() default "";
}
