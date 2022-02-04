package kaufland.com.demo.entity

import kaufland.com.coachbasebinderapi.PersistenceConfig
import kaufland.com.coachbasebinderapi.TypeConversion
import kaufland.com.demo.UnitTestConnector
import org.junit.Assert.*
import org.junit.BeforeClass
import org.junit.Test
import kotlin.reflect.KClass

class ProductWrapperTest {
    companion object {
        private val typeConversions: Map<KClass<*>, TypeConversion> = mapOf(
            ProductCategory::class to ProductCategoryTypeConversion
        )

        @BeforeClass
        @JvmStatic
        fun beforeClass() {
            PersistenceConfig.configure(UnitTestConnector(typeConversions))
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
