package com.kaufland.util;

import java.lang.annotation.Annotation;

import kaufland.com.coachbasebinderapi.Entity;

public class JavaAnnotationUtil {

    public static Entity createReadOnlyCopyOfEntityAnnotation(final Entity source)
    {
        Entity annotation = new Entity()
        {

            @Override
            public Class<? extends Annotation> annotationType() {
                return source.annotationType();
            }

            @Override
            public Class<?> value() {
                return source.value();
            }

            @Override
            public boolean modifierOpen() {
                return source.modifierOpen();
            }

            @Override
            public Type type() {
                return Type.READONLY;
            }

            @Override
            public String database() {
                return source.database();
            }
        };

        return annotation;
    }

}
