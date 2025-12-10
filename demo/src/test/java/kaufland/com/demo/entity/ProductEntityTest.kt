package com.schwarz.crystaldemo.entity

import com.schwarz.crystalapi.PersistenceConfig
import com.schwarz.crystalapi.TypeConversionErrorWrapper
import com.schwarz.crystaldemo.UnitTestConnector
import com.schwarz.crystaldemo.entity.ProductCategory.AMAZING_PRODUCT
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import java.time.LocalDate
import kotlin.system.measureTimeMillis

object LastErrorWrapper {
    var value: TypeConversionErrorWrapper? = null

    fun clear() {
        value = null
    }
}

object ErrorProducingObject

object ProductEntityTestConnector : UnitTestConnector() {
    override fun queryDoc(
        dbName: String,
        queryParams: Map<String, Any>,
        limit: Int?,
        onlyInclude: List<String>?
    ): List<Map<String, Any>> {
        return listOf(queryParams)
    }

    override fun invokeOnError(errorWrapper: TypeConversionErrorWrapper) {
        LastErrorWrapper.value = errorWrapper
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

    @Before
    fun before() {
        LastErrorWrapper.clear()
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

    @Test
    fun `entity with an invalid type for fields without type conversions should produce error on get`() {
        val map: MutableMap<String, Any> =
            mutableMapOf(
                "name" to 1
            )

        val entity = ProductEntity.create(map)

        assertNull(LastErrorWrapper.value)
        assertThrows(Exception::class.java) { entity.name }
    }

    @Test
    fun `entity with an invalid type for list fields without type conversions should produce error on get`() {
        val map: MutableMap<String, Any> =
            mutableMapOf(
                "identifiers" to 1
            )

        val entity = ProductEntity.create(map)
        assertNull(LastErrorWrapper.value)

        val result = entity.identifiers

        assertNull(result)
        val errorWrapper = LastErrorWrapper.value
        assertNotNull(errorWrapper)
        assertEquals("identifiers", errorWrapper?.fieldName)
        assertEquals(1, errorWrapper?.value)
        assertEquals(List::class, errorWrapper?.`class`)
    }

    @Test
    fun `entity with an invalid list type for list fields without type conversions should produce error on get`() {
        val map: MutableMap<String, Any> =
            mutableMapOf(
                "identifiers" to listOf(1)
            )

        val entity = ProductEntity.create(map)

        assertNull(LastErrorWrapper.value)

        val result = entity.identifiers

        assertEquals(emptyList<String>(), result)
        val errorWrapper = LastErrorWrapper.value
        assertNotNull(errorWrapper)
        assertEquals("identifiers", errorWrapper?.fieldName)
        assertEquals(1, errorWrapper?.value)
        assertEquals(String::class, errorWrapper?.`class`)
    }

    @Test
    fun `entity with an invalid type for fields that are subentities produce error on get`() {
        val map: MutableMap<String, Any> =
            mutableMapOf(
                "top_comment" to 1
            )

        val entity = ProductEntity.create(map)

        assertNull(LastErrorWrapper.value)

        val result = entity.topComment

        assertNull(result)
        val errorWrapper = LastErrorWrapper.value
        assertNotNull(errorWrapper)
        assertEquals("top_comment", errorWrapper?.fieldName)
        assertEquals(1, errorWrapper?.value)
        assertEquals(UserCommentWrapper::class, errorWrapper?.`class`)
    }

    @Test
    fun `entity with an invalid list type for fields that are subentities returns emptyList`() {
        val map: MutableMap<String, Any> =
            mutableMapOf(
                "comments" to 1
            )

        val entity = ProductEntity.create(map)

        assertNull(LastErrorWrapper.value)

        val result = entity.comments

        assertNull(result)
        val errorWrapper = LastErrorWrapper.value
        assertNotNull(errorWrapper)
        assertEquals("comments", errorWrapper?.fieldName)
        assertEquals(1, errorWrapper?.value)
        assertEquals(List::class, errorWrapper?.`class`)
    }

    @Test
    fun `entity with an invalid list type for fields that are subentities throws on get`() {
        val map: MutableMap<String, Any> =
            mutableMapOf(
                "comments" to listOf(1)
            )

        val entity = ProductEntity.create(map)

        assertNull(LastErrorWrapper.value)

        val result = entity.comments

        assertEquals(emptyList<UserCommentWrapper>(), result)
        val errorWrapper = LastErrorWrapper.value
        assertNotNull(errorWrapper)
        assertEquals("comments", errorWrapper?.fieldName)
        assertEquals(1, errorWrapper?.value)
        assertEquals(UserCommentWrapper::class, errorWrapper?.`class`)
    }

    @Test
    fun `entity with an invalid type for fields with type conversions should produce error on get`() {
        val map: MutableMap<String, Any> =
            mutableMapOf(
                "some_date" to true
            )

        val entity = ProductEntity.create(map)

        assertNull(LastErrorWrapper.value)

        val result = entity.someDate

        assertNull(result)
        val errorWrapper = LastErrorWrapper.value
        assertNotNull(errorWrapper)
        assertEquals("some_date", errorWrapper?.fieldName)
        assertEquals(true, errorWrapper?.value)
        assertEquals(LocalDate::class, errorWrapper?.`class`)
    }

    @Test
    fun `entity with an invalid list type for fields with type conversions produce error on get`() {
        val map: MutableMap<String, Any> =
            mutableMapOf(
                "some_dates" to listOf(true)
            )

        val entity = ProductEntity.create(map)

        assertNull(LastErrorWrapper.value)

        val result = entity.someDates

        assertEquals(emptyList<LocalDate>(), result)
        val errorWrapper = LastErrorWrapper.value
        assertNotNull(errorWrapper)
        assertEquals("some_dates", errorWrapper?.fieldName)
        assertEquals(true, errorWrapper?.value)
        assertEquals(LocalDate::class, errorWrapper?.`class`)
    }

    @Test
    fun `entity with an invalid value for fields with type conversions should produce error on get`() {
        val map: MutableMap<String, Any> =
            mutableMapOf(
                "some_date" to "foobar"
            )

        val entity = ProductEntity.create(map)

        assertNull(LastErrorWrapper.value)

        val result = entity.someDate

        assertNull(result)
        val errorWrapper = LastErrorWrapper.value
        assertNotNull(errorWrapper)
        assertEquals("some_date", errorWrapper?.fieldName)
        assertEquals("foobar", errorWrapper?.value)
        assertEquals(LocalDate::class, errorWrapper?.`class`)
    }

    @Test
    fun `entity with a list of invalid values for fields with type conversions produce error on get`() {
        val map: MutableMap<String, Any> =
            mutableMapOf(
                "some_dates" to listOf("foobar")
            )

        val entity = ProductEntity.create(map)

        assertNull(LastErrorWrapper.value)

        val result = entity.someDates

        assertEquals(emptyList<LocalDate>(), result)
        val errorWrapper = LastErrorWrapper.value
        assertNotNull(errorWrapper)
        assertEquals("some_dates", errorWrapper?.fieldName)
        assertEquals("foobar", errorWrapper?.value)
        assertEquals(LocalDate::class, errorWrapper?.`class`)
    }

    @Test
    fun `creating an entity with an invalid deserialized type should produce error on get`() {
        val map: MutableMap<String, Any> =
            mutableMapOf(
                "some_date" to ErrorProducingObject
            )

        val entity = ProductEntity.create(map)

        assertNull(LastErrorWrapper.value)

        val result = entity.someDate

        assertNull(result)
        val errorWrapper = LastErrorWrapper.value
        assertNotNull(errorWrapper)
        assertEquals("some_date", errorWrapper?.fieldName)
        assertEquals(ErrorProducingObject, errorWrapper?.value)
        assertEquals(LocalDate::class, errorWrapper?.`class`)
    }

    @Test
    fun `entity with a list of invalid deserialized types produce error on get`() {
        val map: MutableMap<String, Any> =
            mutableMapOf(
                "some_dates" to listOf(ErrorProducingObject)
            )

        val entity = ProductEntity.create(map)

        assertNull(LastErrorWrapper.value)

        val result = entity.someDates

        assertEquals(emptyList<LocalDate>(), result)
        val errorWrapper = LastErrorWrapper.value
        assertNotNull(errorWrapper)
        assertEquals("some_dates", errorWrapper?.fieldName)
        assertEquals(ErrorProducingObject, errorWrapper?.value)
        assertEquals(LocalDate::class, errorWrapper?.`class`)
    }

    @Test
    fun `entity with an valid type for fields without type conversions should return`() {
        val map: MutableMap<String, Any> =
            mutableMapOf(
                "name" to "Fritz"
            )

        val entity = ProductEntity.create(map)

        assertEquals("Fritz", entity.name)
        assertNull(LastErrorWrapper.value)
    }

    @Test
    fun `entity with an valid type for list fields without type conversions should return`() {
        val map: MutableMap<String, Any> =
            mutableMapOf(
                "identifiers" to listOf("1", "2")
            )

        val entity = ProductEntity.create(map)
        assertNull(LastErrorWrapper.value)

        assertEquals(listOf("1", "2"), entity.identifiers)
        assertNull(LastErrorWrapper.value)
    }

    @Test
    fun `entity with an valid type for fields that are subentities should return`() {
        val map: MutableMap<String, Any> =
            mutableMapOf(
                "top_comment" to mapOf("comment" to "foobar")
            )

        val entity = ProductEntity.create(map)
        val result = entity.topComment

        assertNotNull(result)
        assertEquals("foobar", result?.comment)
        assertNull(LastErrorWrapper.value)
    }

    @Test
    fun `entity with an valid list type for fields that are subentities returns`() {
        val map: MutableMap<String, Any> =
            mutableMapOf(
                "comments" to listOf(mapOf("comment" to "foobar"))
            )

        val entity = ProductEntity.create(map)
        val result = entity.comments

        assertNotNull(result)
        assertEquals("foobar", result?.first()?.comment)
        assertNull(LastErrorWrapper.value)
    }

    @Test
    fun `entity with an valid type for fields with type conversions should return`() {
        val map: MutableMap<String, Any> =
            mutableMapOf(
                "some_date" to "2023-01-01"
            )

        val entity = ProductEntity.create(map)
        val result = entity.someDate

        assertNotNull(result)
        assertEquals(LocalDate.of(2023, 1, 1), result)
        assertNull(LastErrorWrapper.value)
    }

    @Test
    fun `entity with an valid list type for fields with type conversions should return`() {
        val map: MutableMap<String, Any> =
            mutableMapOf(
                "some_dates" to listOf("2023-01-01")
            )

        val entity = ProductEntity.create(map)
        val result = entity.someDates

        assertNotNull(result)
        assertEquals(LocalDate.of(2023, 1, 1), result?.first())
        assertNull(LastErrorWrapper.value)
    }

    @Test
    fun `entity with an valid deserialized type for fields with type conversions should return`() {
        val map: MutableMap<String, Any> =
            mutableMapOf(
                "some_date" to LocalDate.of(2023, 1, 1)
            )

        val entity = ProductEntity.create(map)
        val result = entity.someDate

        assertNotNull(result)
        assertEquals(LocalDate.of(2023, 1, 1), result)
        assertNull(LastErrorWrapper.value)
    }

    @Test
    fun `entity with an valid deserialized list type for fields with type conversions should return`() {
        val map: MutableMap<String, Any> =
            mutableMapOf(
                "some_dates" to listOf(LocalDate.of(2023, 1, 1))
            )

        val entity = ProductEntity.create(map)
        val result = entity.someDates

        assertNotNull(result)
        assertEquals(LocalDate.of(2023, 1, 1), result?.first())
        assertNull(LastErrorWrapper.value)
    }

    @Test
    fun `default values and constants should be set`() {
        val entity = ProductEntity.create()

        assertEquals(entity.type, "product")
        assertEquals(entity.fieldWithDefault, "foobar")
        val toMapResult = entity.toMap()
        assertEquals("product", toMapResult["type"])
        assertEquals("foobar", toMapResult["field_with_default"])
    }

    @Test
    fun `default values should be overwritable through setter`() {
        val entity = ProductEntity.create()

        entity.fieldWithDefault = "barfoo"

        assertEquals(entity.fieldWithDefault, "barfoo")
        val toMapResult = entity.toMap()
        assertEquals("barfoo", toMapResult["field_with_default"])
    }

    @Test
    fun `default values should be overwritable from doc`() {
        val map = mutableMapOf<String, Any>("field_with_default" to "barfoo")
        val entity = ProductEntity.create(map)

        assertEquals(entity.fieldWithDefault, "barfoo")
        val toMapResult = entity.toMap()
        assertEquals("barfoo", toMapResult["field_with_default"])
    }

    @Test
    fun `constant values should not be overwritable from doc`() {
        val map = mutableMapOf<String, Any>("type" to "barfoo")
        val entity = ProductEntity.create(map)

        assertEquals(entity.type, "product")
        val toMapResult = entity.toMap()
        assertEquals("product", toMapResult["type"])
    }

    @Test
    fun `additional fields in the map should not impact the entity`() {
        val map = mutableMapOf<String, Any>("type" to "barfoo", "this_field_is_not_in_the_schema" to "1234")
        val entity = ProductEntity.create(map)

        assertEquals(entity.type, "product")
        val toMapResult = entity.toMap()
        assertEquals("product", toMapResult["type"])
    }

    @Test
    fun `creating and reading 1000 docs with 1000 positions should take less than 600ms`() {
        val positions = List(1000) { mutableMapOf("comment" to "$it") }
        val someDates = List(1000) { LocalDate.now().toString() }
        val maps =
            List(1000) {
                mutableMapOf<String, Any>(
                    "comments" to positions,
                    "some_dates" to someDates
                )
            }

        val duration =
            measureTimeMillis {
                val entities = maps.map { ProductEntity.create(it) }
                entities.flatMap { it.comments ?: emptyList() }
                entities.flatMap { it.someDates ?: emptyList() }
            }

        assertTrue("Expecting time for creating and reading to be < 600ms but was $duration", duration < 600)
    }
}
