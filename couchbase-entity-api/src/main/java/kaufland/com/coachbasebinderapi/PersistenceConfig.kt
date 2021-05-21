package kaufland.com.coachbasebinderapi

import kotlin.reflect.KClass

object PersistenceConfig {
    private var mConnector: Connector? = null
    private var mSuspendingConnector: SuspendingConnector? = null

    interface Connector {
        val typeConversions: Map<KClass<*>, TypeConversion>
        fun getDocument(id: String, dbName: String): Map<String, Any>?
        fun getDocuments(ids: List<String>, dbName: String): List<Map<String, Any>?>
        fun queryDoc(dbName: String, queryParams: Map<String, Any>): List<Map<String, Any>>

        @Throws(PersistenceException::class)
        fun deleteDocument(id: String, dbName: String)

        @Throws(PersistenceException::class)
        fun upsertDocument(document: MutableMap<String, Any>, id: String?, dbName: String)
    }

    interface SuspendingConnector{

        val typeConversions: Map<KClass<*>, TypeConversion>

        suspend fun getDocument(id: String, dbName: String): Map<String, Any>?

        suspend fun getDocuments(ids: List<String>, dbName: String): List<Map<String, Any>>

        suspend fun queryDoc(dbName: String, queryParams: Map<String, Any>): List<Map<String, Any>>

        @Throws(PersistenceException::class)
        suspend fun deleteDocument(id: String, dbName: String)

        @Throws(PersistenceException::class)
        suspend fun upsertDocument(document: MutableMap<String, Any>, id: String?, dbName: String)
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

    fun configure(connector: Connector) {
        mConnector = connector
    }

    fun configure(connector: SuspendingConnector) {
        mSuspendingConnector = connector
    }
}