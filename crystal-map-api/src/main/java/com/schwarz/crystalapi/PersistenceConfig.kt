package com.schwarz.crystalapi

object PersistenceConfig {
    private var mConnector: Connector? = null
    private var mSuspendingConnector: SuspendingConnector? = null

    interface Connector : TypeConversionErrorCallback {
        fun getDocument(
            id: String,
            dbName: String,
            onlyInclude: List<String>? = null
        ): Map<String, Any>?

        fun getDocuments(
            ids: List<String>,
            dbName: String,
            onlyInclude: List<String>? = null
        ): List<Map<String, Any>?>

        fun queryDoc(
            dbName: String,
            queryParams: Map<String, Any>,
            limit: Int? = null,
            onlyInclude: List<String>? = null
        ): List<Map<String, Any>>

        @Throws(PersistenceException::class)
        fun deleteDocument(
            id: String,
            dbName: String
        )

        @Throws(PersistenceException::class)
        fun upsertDocument(
            document: MutableMap<String, Any>,
            id: String?,
            dbName: String
        ): Map<String, Any>
    }

    interface SuspendingConnector : TypeConversionErrorCallback {
        suspend fun getDocument(
            id: String,
            dbName: String,
            onlyInclude: List<String>? = null
        ): Map<String, Any>?

        suspend fun getDocuments(
            ids: List<String>,
            dbName: String,
            onlyInclude: List<String>? = null
        ): List<Map<String, Any>>

        suspend fun queryDoc(
            dbName: String,
            queryParams: Map<String, Any>,
            limit: Int? = null,
            onlyInclude: List<String>? = null
        ): List<Map<String, Any>>

        @Throws(PersistenceException::class)
        suspend fun deleteDocument(
            id: String,
            dbName: String
        )

        @Throws(PersistenceException::class)
        suspend fun upsertDocument(
            document: MutableMap<String, Any>,
            id: String?,
            dbName: String
        ): Map<String, Any>
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
