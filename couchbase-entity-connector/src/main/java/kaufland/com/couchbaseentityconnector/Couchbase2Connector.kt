package kaufland.com.couchbaseentityconnector

import com.couchbase.lite.CouchbaseLiteException
import com.couchbase.lite.Database
import com.couchbase.lite.Document
import com.couchbase.lite.MutableDocument

import java.util.ArrayList
import java.util.HashMap

import kaufland.com.coachbasebinderapi.PersistenceConfig
import kaufland.com.coachbasebinderapi.PersistenceException
import kaufland.com.coachbasebinderapi.TypeConversion

abstract class Couchbase2Connector : PersistenceConfig.Connector {

    private val mTypeConversions = HashMap<Class<*>, TypeConversion>()

    protected abstract fun getDatabase(name: String): Database

    init {
        mTypeConversions[Int::class.java] = object : TypeConversion {

            override fun write(value: Any): Any {
                return value
            }

            override fun read(value: Any): Any {
                if (value is Number) {
                    return value.toInt()
                }
                if (value is Iterable<*>) {
                    val result = ArrayList<Any>()
                    for (itValue in value) {
                        itValue?.let {
                            result.add(read(itValue))
                        }
                    }
                    return result
                }
                return value
            }
        }
        mTypeConversions[Double::class.java] = object : TypeConversion {
            override fun write(value: Any): Any {
                return value
            }

            override fun read(value: Any): Any {
                if (value is Number) {
                    return value.toDouble()
                }
                if (value is Iterable<*>) {
                    val result = ArrayList<Any>()
                    for (itValue in value) {
                        itValue?.let {
                            result.add(read(itValue))
                        }
                    }
                    return result
                }
                return value
            }
        }
    }

    override fun getTypeConversions(): Map<Class<*>, TypeConversion> {
        return mTypeConversions
    }

    override fun getDocument(docId: String?, name: String): Map<String, Any> {
        if (docId == null) {
            val document = MutableDocument()
            val result = document.toMap()
            result["_id"] = document.id
            return result
        }

        val document = getDatabase(name).getDocument(docId)
        if (document == null) {
            val result = HashMap<String, Any>()
            result["_id"] = docId
            return result
        }
        val result = document.toMap()
        result["_id"] = docId
        return result
    }

    @Throws(PersistenceException::class)
    override fun deleteDocument(id: String, dbName: String) {

        try {
            val document = getDatabase(dbName).getDocument(id)
            if (document != null) {
                getDatabase(dbName).delete(document)
            }
        } catch (e: CouchbaseLiteException) {
            throw PersistenceException(e)
        }

    }

    @Throws(PersistenceException::class)
    override fun upsertDocument(upsert: MutableMap<String, Any>, docId: String, name: String) {
        if (upsert["_id"] == null) {
            upsert["_id"] = docId
        }
        val unsavedDoc = MutableDocument(docId, upsert)
        try {
            upsert["_id"] = unsavedDoc.id
            unsavedDoc.setString("_id", unsavedDoc.id)
            getDatabase(name).save(unsavedDoc)
        } catch (e: CouchbaseLiteException) {
            throw PersistenceException(e)
        }

    }
}
