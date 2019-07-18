package kaufland.com.coachbasebinderapi;

import java.lang.reflect.Array;
import java.util.Map;
import java.util.stream.Stream;

public class DefaultValue {

    private static final Double DOUBLE_DEFAULT = Double.valueOf(0d);
    private static final Float FLOAT_DEFAULT = Float.valueOf(0f);

    public static <T> T get(Class<T> type) {
        if (type == boolean.class) {
            return (T) Boolean.FALSE;
        } else if (type == char.class) {
            return (T) Character.valueOf('\0');
        } else if (type == byte.class) {
            return (T) Byte.valueOf((byte) 0);
        } else if (type == short.class) {
            return (T) Short.valueOf((short) 0);
        } else if (type == int.class) {
            return (T) Integer.valueOf(0);
        } else if (type == long.class) {
            return (T) Long.valueOf(0L);
        } else if (type == float.class) {
            return (T) FLOAT_DEFAULT;
        } else if (type == double.class) {
            return (T) DOUBLE_DEFAULT;
        } else {
            return null;
        }
    }
}
