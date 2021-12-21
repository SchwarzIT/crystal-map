package kaufland.com.demo.entity

import kaufland.com.coachbasebinderapi.PersistenceConfig
import kaufland.com.coachbasebinderapi.TypeConversion
import kaufland.com.demo.UnitTestConnector
import kaufland.com.demo.entity.ProductCategory.AMAZING_PRODUCT
import org.junit.Assert.*
import org.junit.BeforeClass
import org.junit.Test
import kotlin.reflect.KClass

private val typeConversions: Map<KClass<*>, TypeConversion> = mapOf(
    ProductCategory::class to ProductCategoryTypeConversion
)

object ProductEntityTestConnector : UnitTestConnector(typeConversions) {
    override fun queryDoc(dbName: String, queryParams: Map<String, Any>, limit: Int?): List<Map<String, Any>> {
        return listOf(queryParams) // just return the attributes searched for
    }
}

class ProductEntityTest {
    companion object {

        @BeforeClass
        @JvmStatic
        fun beforeClass() {
            PersistenceConfig.configure(ProductEntityTestConnector)
        }
    }

    @Test
    fun `findByTypeAndCategory should use the expected query-params`() {
        val category = AMAZING_PRODUCT

        val result = ProductEntity.findByTypeAndCategory(category)

        assertEquals(result.size, 1)
        val queryParams = result.first()
        assertEquals(AMAZING_PRODUCT, queryParams.category)
        assertEquals(ProductEntity.DOC_TYPE, queryParams.type)
    }
}