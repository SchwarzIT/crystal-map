package kaufland.com.demo

import kaufland.com.coachbasebinderapi.PersistenceConfig
import kaufland.com.coachbasebinderapi.TypeConversion
import kotlin.reflect.KClass

open class UnitTestConnector(
    override val typeConversions: Map<KClass<*>, TypeConversion> = emptyMap()
) : PersistenceConfig.Connector {
    override fun deleteDocument(id: String, dbName: String) {
        // Do Nothing
    }

    override fun getDocument(id: String, dbName: String): Map<String, Any>? {
        return null
    }

    override fun getDocuments(ids: List<String>, dbName: String): List<Map<String, Any>> {
        return emptyList()
    }

    override fun queryDoc(dbName: String, queryParams: Map<String, Any>, limit: Int?): List<Map<String, Any>> {
        return emptyList()
    }

    override fun upsertDocument(
        document: MutableMap<String, Any>,
        id: String?,
        dbName: String
    ): Map<String, Any> {
        return document
    }
}
