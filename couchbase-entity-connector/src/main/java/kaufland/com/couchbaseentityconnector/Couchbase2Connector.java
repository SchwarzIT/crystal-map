package kaufland.com.couchbaseentityconnector;

import com.couchbase.lite.Blob;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.MutableDocument;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaufland.com.coachbasebinderapi.PersistenceConfig;
import kaufland.com.coachbasebinderapi.PersistenceException;
import kaufland.com.coachbasebinderapi.TypeConversion;

public abstract class Couchbase2Connector implements PersistenceConfig.Connector {

    protected abstract Database getDatabase(String name);

    private Map<Class<?>, TypeConversion> mTypeConversions = new HashMap<>();

    public Couchbase2Connector() {
        mTypeConversions.put(Integer.class, new TypeConversion() {

            @Override
            public Object write(Object value) {
                return value;
            }

            @Override
            public Object read(Object value) {
                if (value instanceof Number) {
                    return ((Number) value).intValue();
                }
                if(value instanceof Iterable){
                    List<Object> result = new ArrayList<>();
                    for (Object itValue : ((Iterable) value)) {
                        result.add(read(itValue));
                    }
                    return result;
                }
                return value;
            }
        });
    }

    @Override
    public Map<Class<?>, TypeConversion> getTypeConversions() {
        return mTypeConversions;
    }

    @Override
    public Map<String, Object> getDocument(String docId, String name) {

        if (docId == null) {
            return new HashMap<>();
        }

        Document document = getDatabase(name).getDocument(docId);
        if (document == null) {
            HashMap<String, Object> result = new HashMap<>();
            result.put("_id", docId);
            return result;
        }
        Map<String, Object> result = document.toMap();
        result.put("_id", docId);
        return result;
    }

    @Override
    public void deleteDocument(String id, String dbName) throws PersistenceException {

        try {
            Document document = getDatabase(dbName).getDocument(id);
            if (document != null) {
                getDatabase(dbName).delete(document);
            }
        } catch (CouchbaseLiteException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void upsertDocument(Map<String, Object> upsert, String docId, String name) throws PersistenceException {

        if (upsert.get("_id") == null) {
            upsert.put("_id", docId);
        }
        MutableDocument unsavedDoc = new MutableDocument(docId, upsert);
        try {
            unsavedDoc.setString("_id", unsavedDoc.getId());
            getDatabase(name).save(unsavedDoc);
        } catch (CouchbaseLiteException e) {
            throw new PersistenceException(e);
        }

    }
}
