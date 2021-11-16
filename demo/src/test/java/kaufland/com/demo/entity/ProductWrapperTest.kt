package kaufland.com.demo.entity

import kaufland.com.coachbasebinderapi.PersistenceConfig
import kaufland.com.coachbasebinderapi.TypeConversion
import org.junit.Assert.*
import org.junit.BeforeClass
import org.junit.Test
import kotlin.reflect.KClass

class ProductWrapperTest {
    companion object {
        @BeforeClass
        @JvmStatic
        fun beforeClass() {
            PersistenceConfig.configure(object : PersistenceConfig.Connector {
                override val typeConversions: Map<KClass<*>, TypeConversion> = mapOf()

                override fun getDocument(id: String, dbName: String): Map<String, Any>? {
                    TODO("Not yet implemented")
                }

                override fun getDocuments(ids: List<String>, dbName: String): List<Map<String, Any>?> {
                    TODO("Not yet implemented")
                }

                override fun queryDoc(dbName: String, queryParams: Map<String, Any>, limit: Int): List<Map<String, Any>> {
                    TODO("Not yet implemented")
                }

                override fun deleteDocument(id: String, dbName: String) {
                    TODO("Not yet implemented")
                }

                override fun upsertDocument(document: MutableMap<String, Any>, id: String?, dbName: String): Map<String, Any> {
                    TODO("Not yet implemented")
                }
            })
        }
    }

    @Test
    fun `toMap should preserve field order`() {
        val product = ProductWrapper.create().builder()
            .setName("name")
            .setComments(emptyList())
            .setIdentifiers(listOf("1", "2"))
            .exit()

        val map = product.toMap()
        map.remove(ProductWrapper.TYPE)
        assertEquals(
            mapOf(
                ProductWrapper.NAME to "name",
                ProductWrapper.COMMENTS to listOf<UserCommentWrapper>(),
                ProductWrapper.IDENTIFIERS to listOf("1", "2")
            ),
            map
        )
    }
}
