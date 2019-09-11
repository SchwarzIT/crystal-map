package kaufland.com.coachbasebinderapi;


import java.util.HashMap;
import java.util.Map;

import kotlin.reflect.KClass;

public class PersistenceConfig {

    private static PersistenceConfig mInstance;

    private final Connector mConnector;

    private PersistenceConfig(Connector connector) {
        mConnector = connector;
    }

    public interface Connector {

        Map<KClass<?>, TypeConversion> getTypeConversions();

        Map<String, Object> getDocument(String id, String dbName);

        void deleteDocument(String id, String dbName) throws PersistenceException;

        void upsertDocument(Map<String, Object> document, String id, String dbName) throws PersistenceException;

    }

    public static void configure(Connector connector) {
        mInstance = new PersistenceConfig(connector);
    }

    public Connector getConnector() {
        if (mConnector == null) {
            throw new RuntimeException("no database connector configured.. call PersistenceConfig.configure(Connector");
        }

        return mConnector;
    }

    public static PersistenceConfig getInstance() {
        return mInstance;
    }
}
