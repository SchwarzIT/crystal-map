package com.kaufland.generation

import com.kaufland.generation.model.KDocGeneration
import org.junit.Test

class KDocGenerationTest {

    @Test
    fun `generateKDoc should not throw exception for formatting placeholders`() {
        KDocGeneration.generate(arrayOf("%1D"))
    }
}
