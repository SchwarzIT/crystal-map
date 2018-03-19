package kaufland.com.coachbasebinderapi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE})
public @interface CblEntity {

    /**
     * The implementation class of the injected bean.
     *
     * @return the implementation class
     */
    Class<?> value() default Void.class;

    String database() default "";

    String id() default "";
}
