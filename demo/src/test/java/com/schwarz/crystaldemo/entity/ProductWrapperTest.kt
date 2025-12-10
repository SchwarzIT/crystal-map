package com.schwarz.crystaldemo.entity

import com.schwarz.crystalapi.PersistenceConfig
import com.schwarz.crystaldemo.UnitTestConnector
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ProductWrapperTest {
    companion object {
        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            PersistenceConfig.configure(UnitTestConnector())
        }
    }

    @Test
    fun `toMap should preserve field order`() {
        val product =
            ProductWrapper
                .create()
                .builder()
                .setName("name")
                .setComments(emptyList())
                .setIdentifiers(listOf("1", "2"))
                .exit()

        val map = product.toMap()
        map.remove(ProductWrapper.TYPE)
        assertEquals(
            mapOf(
                ProductWrapper.FIELD_WITH_DEFAULT to "foobar",
                ProductWrapper.NAME to "name",
                ProductWrapper.COMMENTS to listOf<UserCommentWrapper>(),
                ProductWrapper.IDENTIFIERS to listOf("1", "2"),
            ),
            map,
        )
    }

    @Test
    fun `toMap - fromMap should work`() {
        val product =
            ProductWrapper
                .create()
                .builder()
                .setName("name")
                .setComments(
                    listOf(
                        UserCommentWrapper.create().apply {
                            comment = "foobar"
                        },
                    ),
                ).setCategory(ProductCategory.AMAZING_PRODUCT)
                .setIdentifiers(listOf("1", "2"))
                .setSomeDate(LocalDate.now())
                .exit()
        val map = product.toMap() as MutableMap<String, Any?>

        val result = ProductWrapper.fromMap(map)!!

        assertEquals(product.name, result.name)
        assertEquals(product.comments?.first()?.comment!!, result.comments?.first()?.comment!!)
        assertEquals(product.category, result.category)
        assertEquals(product.identifiers, result.identifiers)
        assertEquals(product.someDate, result.someDate)
    }
}
