package com.schwarz.crystalapi.schema

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DefaultSchemaValidatorTest {

    private lateinit var validator: DefaultSchemaValidator
    private lateinit var logger: TestLogger

    @BeforeEach
    fun setUp() {
        validator = DefaultSchemaValidator()
        logger = TestLogger()
    }

    @Test
    fun `unchanged model logs info did not change`() {
        val schema = entitySchema("TestEntity")
        validator.validate(listOf(schema), listOf(schema), logger)

        assertEquals(listOf("INFO: did not change"), logger.messages)
    }

    @Test
    fun `field changed without deprecatedSchema logs error`() {
        val released = entitySchema("TestEntity", fields = listOf(field("name", "String")))
        val current = entitySchema("TestEntity", fields = listOf(field("name", "Int")))

        validator.validate(listOf(current), listOf(released), logger)

        assertEquals(listOf("ERROR: forbidden change on existing field [name]"), logger.messages)
    }

    @Test
    fun `field changed in deprecatedSchema with inUse true logs error`() {
        val released = entitySchema(
            "TestEntity",
            fields = listOf(field("name", "String")),
            deprecatedSchema = DeprecatedSchema(
                replacedBy = null,
                inUse = true,
                deprecatedFields = listOf(DeprecatedFields("name", null, inUse = true)),
            ),
        )
        val current = entitySchema(
            "TestEntity",
            fields = listOf(field("name", "Int")),
            deprecatedSchema = released.deprecatedSchema,
        )

        validator.validate(listOf(current), listOf(released), logger)

        assertEquals(listOf("ERROR: forbidden change on existing field [name]"), logger.messages)
    }

    @Test
    fun `field changed in deprecatedSchema with inUse false logs allowed`() {
        val released = entitySchema(
            "TestEntity",
            fields = listOf(field("name", "String")),
            deprecatedSchema = DeprecatedSchema(
                replacedBy = null,
                inUse = true,
                deprecatedFields = listOf(DeprecatedFields("name", null, inUse = false)),
            ),
        )
        val current = entitySchema(
            "TestEntity",
            fields = listOf(field("name", "Int")),
            deprecatedSchema = released.deprecatedSchema,
        )

        validator.validate(listOf(current), listOf(released), logger)

        assertEquals(
            listOf("INFO: allowed change on existing field [name] since it's deprecated and no longer in use"),
            logger.messages,
        )
    }

    @Test
    fun `field changed in deprecatedSchema but field not in deprecatedFields logs error`() {
        val released = entitySchema(
            "TestEntity",
            fields = listOf(field("name", "String")),
            deprecatedSchema = DeprecatedSchema(
                replacedBy = null,
                inUse = true,
                deprecatedFields = listOf(DeprecatedFields("otherField", null, inUse = false)),
            ),
        )
        val current = entitySchema(
            "TestEntity",
            fields = listOf(field("name", "Int")),
            deprecatedSchema = released.deprecatedSchema,
        )

        validator.validate(listOf(current), listOf(released), logger)

        assertEquals(listOf("ERROR: forbidden change on existing field [name]"), logger.messages)
    }

    @Test
    fun `model deleted without deprecatedSchema logs error`() {
        val released = entitySchema("TestEntity")

        validator.validate(emptyList(), listOf(released), logger)

        assertEquals(listOf("ERROR: forbidden model deletion"), logger.messages)
    }

    @Test
    fun `model deleted with deprecatedSchema inUse true logs error`() {
        val released = entitySchema(
            "TestEntity",
            deprecatedSchema = DeprecatedSchema(replacedBy = null, inUse = true, deprecatedFields = emptyList()),
        )

        validator.validate(emptyList(), listOf(released), logger)

        assertEquals(listOf("ERROR: forbidden model deletion"), logger.messages)
    }

    @Test
    fun `model deleted with deprecatedSchema inUse false logs allowed`() {
        val released = entitySchema(
            "TestEntity",
            deprecatedSchema = DeprecatedSchema(replacedBy = null, inUse = false, deprecatedFields = emptyList()),
        )

        validator.validate(emptyList(), listOf(released), logger)

        assertEquals(listOf("INFO: allowed model deletion"), logger.messages)
    }

    @Test
    fun `docId scheme changed logs error`() {
        val released = entitySchema("TestEntity", docId = DocId("type::id"))
        val current = entitySchema("TestEntity", docId = DocId("type::newId"))

        validator.validate(listOf(current), listOf(released), logger)

        assertEquals(listOf("ERROR: forbidden DocId Schema change"), logger.messages)
    }

    private fun entitySchema(
        name: String,
        fields: List<Fields> = emptyList(),
        docId: DocId? = null,
        deprecatedSchema: DeprecatedSchema? = null,
    ) = EntitySchema(
        name = name,
        fields = fields,
        basedOn = emptyList(),
        queries = emptyList(),
        docId = docId,
        deprecatedSchema = deprecatedSchema,
    )

    private fun field(dbField: String, fieldType: String) = Fields(
        dbField = dbField,
        fieldType = fieldType,
        isIterable = false,
        isConstant = false,
        defaultValue = "",
        mandatory = null,
    )

    private class TestLogger : SchemaValidationLogger {
        val messages = mutableListOf<String>()

        override fun info(entitySchema: EntitySchema, message: String) {
            messages.add("INFO: $message")
        }

        override fun error(entitySchema: EntitySchema, message: String) {
            messages.add("ERROR: $message")
        }

        override fun warning(entitySchema: EntitySchema, message: String) {
            messages.add("WARNING: $message")
        }
    }
}
