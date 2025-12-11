package com.schwarz.crystalapi.util

import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test

class CrystalWrapTest {
    @Test
    fun `test validate throws exception on null value`() {
        val values = mutableMapOf<String, Any>("foo" to "bar")
        try {
            CrystalWrap.validate(values, arrayOf("foo", "foobar"))
            fail("there should be an exception")
        } catch (e: NullPointerException) {
        }
    }
}
