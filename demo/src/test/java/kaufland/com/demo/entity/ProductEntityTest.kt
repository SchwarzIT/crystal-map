package com.schwarz.crystaldemo.entity

import com.schwarz.crystalapi.PersistenceConfig
import com.schwarz.crystalapi.TypeConversionErrorWrapper
import com.schwarz.crystaldemo.UnitTestConnector
import com.schwarz.crystaldemo.entity.ProductCategory.AMAZING_PRODUCT
import com.schwarz.crystaldemo.logger.TestAppender
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.BeforeClass
import org.junit.Test
import org.mockito.internal.matchers.Null
import org.slf4j.LoggerFactory

private val logger =
    LoggerFactory.getLogger(ProductEntityTestConnector::class.java) as ch.qos.logback.classic.Logger

private val dataTypeErrorMsg: (String?, String?, String?) -> String
    get() = { fieldName, value, `class` ->
        "Field $fieldName manipulated: Tried to cast $value into $`class`"
    }

object ProductEntityTestConnector : UnitTestConnector() {
    init {
        TestAppender().run {
            name = this::class.java.simpleName
            logger.addAppender(this)
            start()
        }
    }

    override fun queryDoc(
        dbName: String,
        queryParams: Map<String, Any>,
        limit: Int?,
        onlyInclude: List<String>?
    ): List<Map<String, Any>> {
        return listOf(queryParams)
    }

    override fun invokeOnError(errorWrapper: TypeConversionErrorWrapper) {
        if (errorWrapper.exception is ClassCastException) {
            logger.error(
                dataTypeErrorMsg.invoke(
                    errorWrapper.fieldName,
                    if (errorWrapper.value != null) {
                        errorWrapper.value?.javaClass?.kotlin?.simpleName ?: ""
                    } else {
                        Null.NULL.toString().lowercase()
                    },
                    errorWrapper.`class`.simpleName
                )
            )
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
    fun `data type changed at runtime test suppress exception`() {
        val map: MutableMap<String, Any> = mutableMapOf(
            "name" to 1
        )
        ProductEntity.create(map)
        assertEquals(
            (logger.getAppender(TestAppender::class.java.simpleName) as TestAppender).lastLoggedEvent?.message,
            dataTypeErrorMsg.invoke("name", 1::class.simpleName, String::class.simpleName)
        )
    }

    /**
     * Can happen if combined db data is changed wilfully.
     */
    @Test
    fun `list data type changed at runtime test suppress exception`() {
        val map: MutableMap<String, Any> = mutableMapOf(
            "identifiers" to 1
        )
        ProductEntity.create(map)
        assertEquals(
            (logger.getAppender(TestAppender::class.java.simpleName) as TestAppender).lastLoggedEvent?.message,
            dataTypeErrorMsg.invoke("identifiers", 1::class.simpleName, List::class.simpleName)
        )
    }

    /**
     * Can happen if combined db data is changed wilfully.
     */
    @Test
    fun `list item data type changed at runtime test suppress exception`() {
        val map: MutableMap<String, Any> = mutableMapOf(
            "identifiers" to listOf(1)
        )
        ProductEntity.create(map)
        assertEquals(
            (logger.getAppender(TestAppender::class.java.simpleName) as TestAppender).lastLoggedEvent?.message,
            dataTypeErrorMsg.invoke("identifiers", "SingletonList", List::class.simpleName)
        )
    }

    @Test
    fun `data type consistent`() {
        val map: MutableMap<String, Any> = mutableMapOf(
            "name" to "1"
        )
        ProductEntity.create(map)
        assertNull((logger.getAppender(TestAppender::class.java.simpleName) as TestAppender).lastLoggedEvent?.message)
    }

    @Test
    fun `list data type consistent`() {
        val map: MutableMap<String, Any> = mutableMapOf(
            "identifiers" to listOf("Foo")
        )
        ProductEntity.create(map)
        assertNull((logger.getAppender(TestAppender::class.java.simpleName) as TestAppender).lastLoggedEvent?.message)
    }
}
