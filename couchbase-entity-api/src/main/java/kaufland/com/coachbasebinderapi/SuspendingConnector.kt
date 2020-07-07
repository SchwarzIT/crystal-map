package kaufland.com.coachbasebinderapi

import kotlin.reflect.KClass

interface SuspendingConnector{

    suspend fun getTypeConversions(): Map<KClass<*>?, TypeConversion?>?

    suspend fun getDocument(id: String?, dbName: String?): Map<String?, Any?>?

    suspend fun queryDoc(dbName: String?, queryParams: Map<String?, Any?>?): List<Map<String?, Any?>?>?

    @Throws(PersistenceException::class)
    suspend fun deleteDocument(id: String?, dbName: String?)

    @Throws(PersistenceException::class)
    suspend fun upsertDocument(document: Map<String?, Any?>?, id: String?, dbName: String?)
}