package com.schwarz.crystalapi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.CLASS)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface GenerateAccessor {

    Class<?> value() default Void.class;

    // We need to explicitly specify this since the information is lost during annotation processing
    boolean isNullableSuspendFun() default false;
}
