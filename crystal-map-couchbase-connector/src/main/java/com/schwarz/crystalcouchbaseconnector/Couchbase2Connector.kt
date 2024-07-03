package com.schwarz.crystalcouchbaseconnector

import com.couchbase.lite.*
import com.schwarz.crystalapi.PersistenceConfig
import com.schwarz.crystalapi.PersistenceException
import java.lang.IllegalStateException
import java.util.*
import kotlin.jvm.Throws

abstract class Couchbase2Connector : PersistenceConfig.Connector {

    protected abstract fun getDatabase(name: String): Database

    override fun getDocument(id: String, dbName: String, collection: String, onlyInclude: List<String>?): Map<String, Any>? {
        val document = getDocument(id, dbName, collection) ?: return null

        val result = document.toMap()
        result["_id"] = id
        return result
    }

    override fun getDocuments(
        ids: List<String>,
        dbName: String,
        collection: String,
        onlyInclude: List<String>?
    ): List<Map<String, Any>?> =
        ids.mapNotNull { docId ->
            getDocument(docId, dbName, collection, null)
        }

    @Throws(PersistenceException::class)
    override fun deleteDocument(id: String, dbName: String, collection: String) {
        try {
            val document = getDocument(id, dbName, collection)
            if (document != null) {
                if (collection.isEmpty()) {
                    getDatabase(dbName).delete(document)
                } else {
                    getDatabase(dbName).getCollection(collection)?.delete(document)
                }
            }
        } catch (e: CouchbaseLiteException) {
            throw PersistenceException(e)
        }
    }

    @Throws(PersistenceException::class)
    override fun queryDoc(
        dbName: String,
        collection: String,
        queryParams: Map<String, Any>,
        limit: Int?,
        onlyInclude: List<String>?
    ): List<Map<String, Any>> {
        try {
            val builder = QueryBuilder.select(SelectResult.expression(Meta.id), SelectResult.all()).let { select ->
                if (collection.isEmpty()) {
                    select.from(DataSource.database(getDatabase(dbName)))
                } else {
                    select.from(
                        DataSource.collection(
                            getDatabase(dbName).getCollection(collection)
                                ?: throw IllegalStateException("Collection $collection not found for db $dbName")
                        )
                    )
                }
            }

            parseExpressions(queryParams)?.let {
                builder.where(it)
            }

            limit?.let { builder.limit(Expression.intValue(limit)) }

            return queryResultToMap(builder.execute())
        } catch (e: CouchbaseLiteException) {
            throw PersistenceException(e)
        }
    }

    private fun queryResultToMap(execute: ResultSet): List<Map<String, Any>> {
        val parsed: MutableList<Map<String, Any>> =
            ArrayList()
        if (execute != null) {
            for (result in execute) {
                val item: MutableMap<String, Any> = mutableMapOf()
                item["_id"] = result.getString(0).orEmpty()
                item.putAll(result.getDictionary(1)?.toMap() ?: emptyMap())
                parsed.add(item)
            }
        }
        return parsed
    }

    private fun parseExpressions(queryParams: Map<String, Any?>): Expression? {
        var result: Expression? = null

        for (queryParam in queryParams) {
            val equalTo = Expression.property(queryParam.key).equalTo(
                Expression.value(queryParam.value)
            )
            result = if (result == null) {
                equalTo
            } else {
                result.and(equalTo)
            }
        }

        return result
    }

    @Throws(PersistenceException::class)
    override fun upsertDocument(document: MutableMap<String, Any>, id: String?, dbName: String, collection: String): Map<String, Any> {
        if (document["_id"] == null && id != null) {
            document["_id"] = id
        }
        val unsavedDoc = MutableDocument(id, document)
        return try {
            document["_id"] = unsavedDoc.id
            unsavedDoc.setString("_id", unsavedDoc.id)
            if (collection.isEmpty()) {
                getDatabase(dbName).save(unsavedDoc)
            } else {
                getDatabase(dbName).getCollection(collection)?.save(unsavedDoc)
                    ?: throw IllegalStateException("Collection $collection not found for db $dbName")
            }
            document
        } catch (e: CouchbaseLiteException) {
            throw PersistenceException(e)
        }
    }

    private fun getDocument(id: String, dbName: String, collection: String): Document? =
        if (collection.isEmpty()) {
            getDatabase(dbName).getDocument(id) ?: null
        } else {
            getDatabase(dbName).getCollection(collection)?.getDocument(id) ?: null
        }
}
