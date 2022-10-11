package kaufland.com.demo.entity

import kaufland.com.coachbasebinderapi.PersistenceConfig
import kaufland.com.coachbasebinderapi.TypeConversion
import kaufland.com.demo.UnitTestConnector
import kaufland.com.demo.entity.ProductCategory.AMAZING_PRODUCT
import kaufland.com.demo.logger.TestAppender
import org.junit.Assert.assertEquals
import org.junit.BeforeClass
import org.junit.Test
import org.mockito.internal.matchers.Null
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass


private val typeConversions: Map<KClass<*>, TypeConversion> = mapOf(ProductCategory::class to ProductCategoryTypeConversion)
private val logger = LoggerFactory.getLogger(ProductEntityTestConnector::class.java) as ch.qos.logback.classic.Logger

private val dataTypeErrorMsg: (String?, String?) -> String
    get() = { value, `class` ->
        "Data type manipulated: Tried to cast $value into $`class`"
    }

object ProductEntityTestConnector : UnitTestConnector(typeConversions) {
    init {
        TestAppender().run {
            name = this::class.java.simpleName
            logger.addAppender(this)
            start()
        }
    }

    override fun queryDoc(dbName: String, queryParams: Map<String, Any>, limit: Int?, onlyInclude: List<String>?): List<Map<String, Any>> {
        return listOf(queryParams)
    }

    override fun invokeOnError(ex: Exception, value: Any?, `class`: KClass<*>) {
        if (ex is ClassCastException) {

            logger.error(dataTypeErrorMsg.invoke(if (value != null) {
                value.javaClass.kotlin.simpleName
            } else {
                Null.NULL.toString().lowercase()
            }, `class`.simpleName))
        }
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

    /**
     * Can happen if combined db data is changed wilfully.
     */
    @Test
    fun `data types changed at runtime Test suppress exception`() {
        ProductEntity.write<String>(1, String::class)
        assertEquals((logger.getAppender(TestAppender::class.java.simpleName) as TestAppender)
                .lastLoggedEvent?.message, dataTypeErrorMsg.invoke(1::class.simpleName, String::class.simpleName))
    }
}
