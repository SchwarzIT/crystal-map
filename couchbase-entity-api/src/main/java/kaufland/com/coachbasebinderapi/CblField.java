package kaufland.com.coachbasebinderapi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by sbra0902 on 26.05.17.
 */

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface CblField {


    String value() default "";

    String attachmentType() default "";

}
