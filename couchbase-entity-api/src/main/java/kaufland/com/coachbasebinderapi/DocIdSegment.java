package kaufland.com.coachbasebinderapi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD})
public @interface DocIdSegment {

    Class<?> value() default Void.class;
}
