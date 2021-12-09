package kaufland.com.coachbasebinderapi;

import java.lang.Exception;

public class PersistenceException extends Exception {
    public PersistenceException(Throwable var1) {
        super(var1);
    }
    public PersistenceException(String message) {
        super(message);
    }
}
