package com.schwarz.crystalapi

import kotlin.reflect.KClass

object PersistenceConfig {
    private var mConnector: Connector? = null
    private var mSuspendingConnector: SuspendingConnector? = null

    interface Connector : TypeConversionErrorCallback {
        val typeConversions: Map<KClass<*>, TypeConversion>
        fun getDocument(
            id: String,
            dbName: String,
            collection: String,
            onlyInclude: List<String>? = null
        ): Map<String, Any>?

        fun getDocuments(
            ids: List<String>,
            dbName: String,
            collection: String,
            onlyInclude: List<String>? = null
        ): List<Map<String, Any>?>

        fun queryDoc(
            dbName: String,
            collection: String,
            queryParams: Map<String, Any>,
            limit: Int? = null,
            onlyInclude: List<String>? = null
        ): List<Map<String, Any>>

        @Throws(PersistenceException::class)
        fun deleteDocument(
            id: String,
            dbName: String,
            collection: String = ""
        )

        @Throws(PersistenceException::class)
        fun upsertDocument(
            document: MutableMap<String, Any>,
            id: String?,
            dbName: String,
            collection: String = ""
        ): Map<String, Any>

        // Compatibility functions without collections

        fun getDocument(
            id: String,
            dbName: String,
            onlyInclude: List<String>? = null
        ): Map<String, Any>? = getDocument(id, dbName, "", onlyInclude)

        fun getDocuments(
            ids: List<String>,
            dbName: String,
            onlyInclude: List<String>? = null
        ): List<Map<String, Any>?> = getDocuments(ids, dbName, "", onlyInclude)

        fun queryDoc(
            dbName: String,
            queryParams: Map<String, Any>,
            limit: Int? = null,
            onlyInclude: List<String>? = null
        ): List<Map<String, Any>> = queryDoc(dbName, "", queryParams, limit, onlyInclude)

    }

    interface SuspendingConnector : TypeConversionErrorCallback {

        val typeConversions: Map<KClass<*>, TypeConversion>

        suspend fun getDocument(
            id: String,
            dbName: String,
            collection: String,
            onlyInclude: List<String>? = null
        ): Map<String, Any>?

        suspend fun getDocuments(
            ids: List<String>,
            dbName: String,
            collection: String,
            onlyInclude: List<String>? = null
        ): List<Map<String, Any>>

        suspend fun queryDoc(
            dbName: String,
            collection: String,
            queryParams: Map<String, Any>,
            limit: Int? = null,
            onlyInclude: List<String>? = null
        ): List<Map<String, Any>>

        @Throws(PersistenceException::class)
        suspend fun deleteDocument(
            id: String,
            dbName: String,
            collection: String = ""
        )

        @Throws(PersistenceException::class)
        suspend fun upsertDocument(
            document: MutableMap<String, Any>,
            id: String?,
            dbName: String,
            collection: String = ""
        ): Map<String, Any>

        // Compatibility functions without collections

        suspend fun getDocument(
            id: String,
            dbName: String,
            onlyInclude: List<String>? = null
        ): Map<String, Any>? = getDocument(id, dbName, "", onlyInclude)

        suspend fun getDocuments(
            ids: List<String>,
            dbName: String,
            onlyInclude: List<String>? = null
        ): List<Map<String, Any>> = getDocuments(ids, dbName, "", onlyInclude)

        suspend fun queryDoc(
            dbName: String,
            queryParams: Map<String, Any>,
            limit: Int? = null,
            onlyInclude: List<String>? = null
        ): List<Map<String, Any>> = queryDoc(dbName, "", queryParams, limit, onlyInclude)
    }

    val connector: Connector
        get() {
            if (mConnector == null) {
                throw RuntimeException("no database connector configured.. call PersistenceConfig.configure(Connector)")
            }
            return mConnector!!
        }

    val suspendingConnector: SuspendingConnector
        get() {
            if (mSuspendingConnector == null) {
                throw RuntimeException("no database suspendingConnector configured.. call PersistenceConfig.configure(SuspendingConnector)")
            }
            return mSuspendingConnector!!
        }

    fun getTypeConversion(type: KClass<*>): TypeConversion? {
        if (mConnector != null) {
            return connector.typeConversions[type]
        } else if (mSuspendingConnector != null) {
            return suspendingConnector.typeConversions[type]
        }
        throw RuntimeException("no database connector configured.. call PersistenceConfig.configure")
    }

    fun onTypeConversionError(errorWrapper: TypeConversionErrorWrapper) {
        (mConnector ?: mSuspendingConnector)?.let {
            it.invokeOnError(errorWrapper)
        }
    }

    fun configure(connector: Connector) {
        mConnector = connector
    }

    fun configure(connector: SuspendingConnector) {
        mSuspendingConnector = connector
    }
}
