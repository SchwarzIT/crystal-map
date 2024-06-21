package com.schwarz.crystalapi.typeconverters

import com.schwarz.crystalapi.ITypeConverter
import junit.framework.TestCase.assertTrue
import org.junit.Test

enum class TestEnum {
    FOO, BAR
}

object TestEnumConverter : ITypeConverter<TestEnum, String> by EnumConverter(TestEnum::class)

class EnumConverterTest {

    @Test
    fun `should correctly read an enum value`() {
        val result = TestEnumConverter.read("FOO")

        assertTrue(result == TestEnum.FOO)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `should throw an exception for incorrect string value`() {
        TestEnumConverter.read("FOZ")
    }

    @Test
    fun `should read null for null string value`() {
        val result = TestEnumConverter.read(null)

        assertTrue(result == null)
    }

    @Test
    fun `should correctly write an enum value`() {
        val result = TestEnumConverter.write(TestEnum.FOO)

        assertTrue(result == "FOO")
    }

    @Test
    fun `should write null for null enum value`() {
        val result = TestEnumConverter.write(null)

        assertTrue(result == null)
    }
}
