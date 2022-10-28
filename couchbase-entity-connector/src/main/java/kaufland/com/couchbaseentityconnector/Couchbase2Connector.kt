package kaufland.com.couchbaseentityconnector

import com.couchbase.lite.*
import kaufland.com.coachbasebinderapi.PersistenceConfig
import kaufland.com.coachbasebinderapi.PersistenceException
import kaufland.com.coachbasebinderapi.TypeConversion
import java.util.*
import kotlin.jvm.Throws
import kotlin.reflect.KClass

abstract class Couchbase2Connector : PersistenceConfig.Connector {

    private val mTypeConversions = HashMap<KClass<*>, TypeConversion>()

    protected abstract fun getDatabase(name: String): Database

    init {
        mTypeConversions[Int::class] = object : TypeConversion {

            override fun write(value: Any?): Any? {
                return value
            }

            override fun read(value: Any?): Any? {
                if (value is Number) {
                    return value.toInt()
                }
                if (value is Iterable<*>) {
                    val result = ArrayList<Any>()
                    for (itValue in value) {
                        itValue?.let {
                            read(itValue)?.let { it1 -> result.add(it1) }
                        }
                    }
                    return result
                }
                return value
            }
        }
        mTypeConversions[Double::class] = object : TypeConversion {
            override fun write(value: Any?): Any? {
                return value
            }

            override fun read(value: Any?): Any? {
                if (value is Number) {
                    return value.toDouble()
                }
                if (value is Iterable<*>) {
                    val result = ArrayList<Any>()
                    for (itValue in value) {
                        itValue?.let {
                            read(itValue)?.let { it1 -> result.add(it1) }
                        }
                    }
                    return result
                }
                return value
            }
        }
    }

    override val typeConversions: Map<KClass<*>, TypeConversion> = mTypeConversions

    override fun getDocument(id: String, dbName: String, onlyInclude: List<String>?): Map<String, Any>? {
        val document = getDatabase(dbName).getDocument(id) ?: return null

        val result = document.toMap()
        result["_id"] = id
        return result
    }

    override fun getDocuments(
        ids: List<String>,
        dbName: String,
        onlyInclude: List<String>?
    ): List<Map<String, Any>?> =
        ids.mapNotNull { docId ->
            getDocument(docId, dbName)
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
    override fun queryDoc(
        dbName: String,
        queryParams: Map<String, Any>,
        limit: Int?,
        onlyInclude: List<String>?
    ): List<Map<String, Any>> {
        try {

            val builder = QueryBuilder.select(SelectResult.expression(Meta.id), SelectResult.all())
                .from(DataSource.database(getDatabase(dbName)))

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
                val item: MutableMap<String, Any> =
                    HashMap()
                item["_id"] = result.getString(0)
                item.putAll(result.getDictionary(1).toMap())
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
    override fun upsertDocument(document: MutableMap<String, Any>, id: String?, dbName: String): Map<String, Any> {
        if (document["_id"] == null && id != null) {
            document["_id"] = id
        }
        val unsavedDoc = MutableDocument(id, document)
        return try {
            document["_id"] = unsavedDoc.id
            unsavedDoc.setString("_id", unsavedDoc.id)
            getDatabase(dbName).save(unsavedDoc)
            document
        } catch (e: CouchbaseLiteException) {
            throw PersistenceException(e)
        }
    }
}
