package kaufland.com.coachbasebinderapi.query;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import kaufland.com.coachbasebinderapi.Field;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE})
public @interface Queries {

    Query[] value() default {};
}
