package com.kaufland

import com.google.testing.compile.Compilation
import com.google.testing.compile.JavaFileObjects
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.junit.Assert
import org.junit.Test
import java.util.*

class CouchbaseBaseBinderProcessorKotlinTest{


    @Test
    fun testKotlinAbstractGeneration() {

        val subEntity = SourceFile.kotlin("Sub.kt", "package com.kaufland.testModels\n" +
                "\n" +
                "import kaufland.com.coachbasebinderapi.Entity\n" +
                "import kaufland.com.coachbasebinderapi.Field\n" +
                "import kaufland.com.coachbasebinderapi.Fields\n" +
                "\n" +
                "@Entity\n" +
                "@Fields(\n" +
                "Field(name = \"test\", type = String::class),\n" +
                "Field(name = \"type\", type = String::class, defaultValue = Sub.TYPE, readonly = true)\n" +
                ")\n" +
                "abstract class Sub {\n" +
                "\n" +
                " companion object {\n" +
                "        const val TYPE: String = \"DWG\"" +
                "}\n" +
                " abstract var test : String?\n" +
                "}")


        val compilation = compileKotlin(subEntity)


        Assert.assertEquals(compilation.exitCode, KotlinCompilation.ExitCode.OK)
    }

    private fun compileKotlin(vararg sourceFiles : SourceFile): KotlinCompilation.Result {
        return KotlinCompilation().apply {
            sources = sourceFiles.toList()

            // pass your own instance of an annotation processor
            annotationProcessors = listOf(CoachBaseBinderProcessor())

            inheritClassPath = true
            messageOutputStream = System.out // see diagnostics in real time
        }.compile()
    }

}
