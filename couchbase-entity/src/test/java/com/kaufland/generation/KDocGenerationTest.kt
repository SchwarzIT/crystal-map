package com.kaufland.generation

import org.junit.Test

class KDocGenerationTest {

    @Test
    fun `generateKDoc should not throw exception for formatting placeholders`() {
        KDocGeneration.generate(arrayOf("%1D"))
    }
}
