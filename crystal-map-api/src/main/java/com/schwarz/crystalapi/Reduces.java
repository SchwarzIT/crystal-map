package com.schwarz.crystalapi;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@Retention(RetentionPolicy.RUNTIME)
public @interface Reduces {

    Reduce[] value();
}
