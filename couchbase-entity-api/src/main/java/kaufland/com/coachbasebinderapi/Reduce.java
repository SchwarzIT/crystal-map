package kaufland.com.coachbasebinderapi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Reduce {

    String namePrefix();

    String[] include();

    boolean includeQueries() default true;

    boolean includeAccessors() default true;

    boolean includeDocId() default true;

    boolean includeBasedOn() default true;
}
