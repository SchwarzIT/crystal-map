package com.schwarz.crystalcore.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class EmbeddedModelTest {
    @Test
    fun `round trip survives html comment terminators and unicode`() {
        val json = """{"a":"content with --> terminator and unicode ✅"}"""

        val embedded = EmbeddedModel.embed("<!--model:", "-->", json)
        val document = "<html>\n<body>x</body>\n</html>\n$embedded\n"

        assertEquals(json, EmbeddedModel.extract(document, "<!--model:", "-->"))
    }

    @Test
    fun `round trip with line comment prefix`() {
        val json = """{"nodes":{"A":"<table></table>"}}"""

        val embedded = EmbeddedModel.embed("// model:", "", json)
        val document = "graph ER {\n}\n$embedded\n"

        assertEquals(json, EmbeddedModel.extract(document, "// model:", ""))
    }

    @Test
    fun `extract returns null when no model comment present`() {
        assertNull(EmbeddedModel.extract("<html></html>", "<!--model:", "-->"))
    }

    @Test
    fun `extract returns null for corrupt payload`() {
        assertNull(EmbeddedModel.extract("<!--model:not!!valid@@base64-->", "<!--model:", "-->"))
    }
}
