package kaufland.com.coachbasebinderapi;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@Retention(RetentionPolicy.RUNTIME)
public @interface Reduces {

    Reduce[] value();
}
