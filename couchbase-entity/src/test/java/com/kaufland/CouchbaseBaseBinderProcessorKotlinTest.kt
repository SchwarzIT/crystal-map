package com.kaufland

import com.kaufland.testdata.TestDataHelper
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.junit.Assert
import org.junit.Test

class CouchbaseBaseBinderProcessorKotlinTest {

    @Test
    fun testSucessSimpleMapper() {
        val compilation = compileKotlin(TestDataHelper.clazzAsJavaFileObjects("SimpleMapperTest"))

        Assert.assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
    }

    @Test
    fun testSucessMapperWithGetterAndSetter() {
        val compilation = compileKotlin(TestDataHelper.clazzAsJavaFileObjects("MapperWithGetterAndSetter"))

        Assert.assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
    }

    @Test
    fun testSucessMapperWithTypeParam() {
        val compilation = compileKotlin(TestDataHelper.clazzAsJavaFileObjects("MapperWithTypeParam"))

        Assert.assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
    }

    @Test
    fun testSucessMapperWithNullable() {
        val compilation = compileKotlin(TestDataHelper.clazzAsJavaFileObjects("MapperWithNullable"))

        Assert.assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
    }

    @Test
    fun testSucessWithQueries() {
        val compilation = compileKotlin(TestDataHelper.clazzAsJavaFileObjects("EntityWithQueries"))

        Assert.assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
    }

    @Test
    fun testSucessWithQueriesAndEnums() {
        val compilation = compileKotlin(TestDataHelper.clazzAsJavaFileObjects("EntityWithQueriesAndEnums"))

        Assert.assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
    }

    @Test
    fun testSucessWithGenerateAccessor() {
        val compilation = compileKotlin(TestDataHelper.clazzAsJavaFileObjects("EntityWithGenerateAccessor"))

        Assert.assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
    }

    @Test
    fun testSucessWithQueriesAndSuspendFunctions() {
        val compilation = compileKotlin(TestDataHelper.clazzAsJavaFileObjects("EntityWithQueries"), useSuspend = true)

        Assert.assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
    }

    @Test
    fun testSucessWithGenerateAccessorAndSuspendFunctions() {
        val compilation = compileKotlin(TestDataHelper.clazzAsJavaFileObjects("EntityWithGenerateAccessor"), useSuspend = true)

        Assert.assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
    }

    @Test
    fun testSuccessDeprecatedGeneration() {
        val compilation = compileKotlin(TestDataHelper.clazzAsJavaFileObjects("EntityWithDeprecatedFields"), useSuspend = true)
        Assert.assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
    }

    @Test
    fun testSuccessDocIdGeneration() {
        val compilation = compileKotlin(TestDataHelper.clazzAsJavaFileObjects("EntityWithDocId"), useSuspend = true)
        Assert.assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
    }

    @Test
    fun testSuccessDocIdSegmentGeneration() {
        val compilation = compileKotlin(TestDataHelper.clazzAsJavaFileObjects("EntityWithDocIdAndDocIdSegments"), useSuspend = true)
        Assert.assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
    }

    @Test
    fun testFailedWrongDeprecatedGeneration() {
        val compilation = compileKotlin(TestDataHelper.clazzAsJavaFileObjects("EntityWithWrongConfiguredDeprecatedFields"), useSuspend = true)
        Assert.assertEquals(compilation.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
        Assert.assertTrue(compilation.messages.contains("replacement [name2] for field [name] does not exists"))
    }

    @Test
    fun testKotlinAbstractGeneration() {

        val subEntity = SourceFile.kotlin(
            "Sub.kt",
            ENTITY_HEADER +
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
                "}"
        )

        val compilation = compileKotlin(subEntity)

        Assert.assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
    }

    @Test
    fun testKotlinAbstractGenerationWithLongFields() {

        val subEntity = SourceFile.kotlin(
            "Sub.kt",
            ENTITY_HEADER +
                "@Entity\n" +
                "@Fields(\n" +
                "Field(name = \"test_test_test\", type = String::class),\n" +
                "Field(name = \"type\", type = String::class, defaultValue = Sub.TYPE, readonly = true)\n" +
                ")\n" +
                "abstract class Sub {\n" +
                "\n" +
                " companion object {\n" +
                "        const val TYPE: String = \"DWG\"" +
                "}\n" +
                " abstract var testTestTest : String?\n" +
                "}"
        )

        val compilation = compileKotlin(subEntity)

        Assert.assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
    }

    @Test
    fun testKotlinPrivateGeneration() {

        val subEntity = SourceFile.kotlin(
            "Sub.kt",
            ENTITY_HEADER +
                "@Entity\n" +
                "@Fields(\n" +
                "Field(name = \"test\", type = String::class),\n" +
                "Field(name = \"type\", type = String::class, defaultValue = Sub.TYPE, readonly = true)\n" +
                ")\n" +
                "class Sub {\n" +
                "\n" +
                " companion object {\n" +
                "        const val TYPE: String = \"DWG\"" +
                "}\n" +
                "}"
        )

        val compilation = compileKotlin(subEntity)

        Assert.assertEquals(compilation.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
        Assert.assertTrue(compilation.messages.contains("Entity can not be final"))
    }

    @Test
    fun testKotlinConstructorFailGeneration() {

        val subEntity = SourceFile.kotlin(
            "Sub.kt",
            ENTITY_HEADER +
                "@Entity\n" +
                "@Fields(\n" +
                "Field(name = \"test\", type = String::class),\n" +
                "Field(name = \"type\", type = String::class, defaultValue = Sub.TYPE, readonly = true)\n" +
                ")\n" +
                "open class Sub(a : String) {\n" +
                "\n" +
                " companion object {\n" +
                "        const val TYPE: String = \"DWG\"" +
                "}\n" +
                "}"
        )

        val compilation = compileKotlin(subEntity)

        Assert.assertEquals(compilation.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
        Assert.assertTrue(compilation.messages.contains("Entity should not have a contructor"))
    }

    private fun compileKotlin(vararg sourceFiles: SourceFile, useSuspend: Boolean = false): KotlinCompilation.Result {
        return KotlinCompilation().apply {
            sources = sourceFiles.toList()

            // pass your own instance of an annotation processor
            annotationProcessors = listOf(CoachBaseBinderProcessor())
            correctErrorTypes = true

            kaptArgs["useSuspend"] = useSuspend.toString()
            inheritClassPath = true
            messageOutputStream = System.out // see diagnostics in real time
        }.compile()
    }

    companion object {
        const val ENTITY_HEADER: String =
            "package com.kaufland.testModels\n" +
                "\n" +
                "import kaufland.com.coachbasebinderapi.Entity\n" +
                "import kaufland.com.coachbasebinderapi.Field\n" +
                "import kaufland.com.coachbasebinderapi.Fields\n"
    }
}
