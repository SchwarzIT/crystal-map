package com.schwarz.crystalapi.deprecated;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE})
public @interface Deprecated {

    Class<?> replacedBy() default Void.class;

    boolean inUse() default true;

    DeprecatedField[] fields() default {};

}
