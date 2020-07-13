package kaufland.com.coachbasebinderapi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE})
public @interface Entity {

    /**
     * The implementation class of the injected bean.
     *
     * @return the implementation class
     */
    enum Type {
        READ_AND_WRITE,
        READONLY
    }

    Class<?> value() default Void.class;

    Type type() default Type.READ_AND_WRITE;

    String database() default "";
}
