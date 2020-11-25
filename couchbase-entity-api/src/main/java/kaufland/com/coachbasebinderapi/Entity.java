package kaufland.com.coachbasebinderapi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE})
public @interface Entity {

    enum Type {
        READ_AND_WRITE,
        READONLY
    }

    Class<?> value() default Void.class;

    boolean modifierOpen() default false;

    Type type() default Type.READ_AND_WRITE;

    String database() default "";
}
