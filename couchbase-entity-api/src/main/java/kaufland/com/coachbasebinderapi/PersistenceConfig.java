package kaufland.com.coachbasebinderapi;

import com.couchbase.lite.Database;
import com.couchbase.lite.Document;

public class PersistenceConfig {

    private static PersistenceConfig mInstance;

    private final DatabaseGet mDatabaseGet;

    private PersistenceConfig(DatabaseGet databaseGet) {
        mDatabaseGet = databaseGet;
    }

    public interface DatabaseGet {
        Database getDatabase();
    }

    public static void configure(DatabaseGet databaseGet) {

        mInstance = new PersistenceConfig(databaseGet);
    }

    public Document createOrGet(String docId) {


        Document doc = mDatabaseGet.getDatabase().getDocument(docId);

        if (doc == null) {
            doc = mDatabaseGet.getDatabase().createDocument();
        }

        return doc;
    }

    public static PersistenceConfig getInstance() {
        return mInstance;
    }
}
