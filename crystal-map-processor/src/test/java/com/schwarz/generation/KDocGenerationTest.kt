package com.schwarz.generation

import com.schwarz.crystalcore.generation.model.KDocGeneration
import org.junit.Test

class KDocGenerationTest {
    @Test
    fun `generateKDoc should not throw exception for formatting placeholders`() {
        KDocGeneration.generate(arrayOf("%1D"))
    }
}
