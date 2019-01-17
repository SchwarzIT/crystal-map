package kaufland.com.couchbaseentityconnector;

import com.couchbase.lite.Blob;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.MutableDocument;

import java.util.HashMap;
import java.util.Map;

import kaufland.com.coachbasebinderapi.PersistenceConfig;
import kaufland.com.coachbasebinderapi.PersistenceException;

public abstract class Couchbase21Connector implements PersistenceConfig.Connector {

    protected abstract Database getDatabase(String name);


    @Override
    public Map<String, Object> getDocument(String docId, String name) {

        if(docId == null){
            return new HashMap<>();
        }

        Document document = getDatabase(name).getDocument(docId);
        if(document == null){
            return new HashMap<>();
        }
        return document.toMap();
    }

    @Override
    public void deleteDocument(String id, String dbName) throws PersistenceException {

        try {
            getDatabase(dbName).delete(getDatabase(dbName).getDocument(id));
        } catch (CouchbaseLiteException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void upsertDocument(Map<String, Object> upsert, String docId, String name) throws PersistenceException {

        MutableDocument unsavedDoc = new MutableDocument(docId, upsert);
        try {
            unsavedDoc.setString("_id", unsavedDoc.getId());
            getDatabase(name).save(unsavedDoc);
        } catch (CouchbaseLiteException e) {
            throw new PersistenceException(e);
        }

    }
}
