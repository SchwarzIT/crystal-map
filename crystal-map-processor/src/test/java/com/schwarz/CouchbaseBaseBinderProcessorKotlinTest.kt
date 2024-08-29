package com.schwarz

import com.schwarz.crystalapi.ITypeConverter
import com.schwarz.crystalapi.TypeConverter
import com.schwarz.crystalprocessor.CoachBaseBinderProcessor
import com.schwarz.testdata.TestDataHelper
import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.Assert
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCompilerApi::class)
class CouchbaseBaseBinderProcessorKotlinTest {

    @Test
    fun testSuccessSimpleMapper() {
        val compilation = compileKotlin(TestDataHelper.clazzAsJavaFileObjects("SimpleMapperTest"))

        Assert.assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
    }

    @Test
    fun testSuccessSimpleReduce() {
        val compilation =
            compileKotlin(TestDataHelper.clazzAsJavaFileObjects("EntityWithSimpleReduce"))

        Assert.assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
    }

    @Test
    fun testSuccessMapperWithGetterAndSetter() {
        val compilation =
            compileKotlin(TestDataHelper.clazzAsJavaFileObjects("MapperWithGetterAndSetter"))

        Assert.assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
    }

    @Test
    fun testSuccessMapperWithTypeParam() {
        val compilation =
            compileKotlin(TestDataHelper.clazzAsJavaFileObjects("MapperWithTypeParam"))

        Assert.assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
    }

    @Test
    fun testSuccessMapperWithNullable() {
        val compilation = compileKotlin(TestDataHelper.clazzAsJavaFileObjects("MapperWithNullable"))

        Assert.assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
    }

    @Test
    fun testSuccessWithQueries() {
        val compilation = compileKotlin(TestDataHelper.clazzAsJavaFileObjects("EntityWithQueries"))

        Assert.assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
    }

    @Test
    fun testSucessWithQueriesAndEnums() {
        val compilation =
            compileKotlin(TestDataHelper.clazzAsJavaFileObjects("EntityWithQueriesAndEnums"))

        Assert.assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
    }

    @Test
    fun testSuccessWithGenerateAccessor() {
        val compilation =
            compileKotlin(TestDataHelper.clazzAsJavaFileObjects("EntityWithGenerateAccessor"))

        Assert.assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
    }

    @Test
    fun testSuccessWithQueriesAndSuspendFunctions() {
        val compilation = compileKotlin(
            TestDataHelper.clazzAsJavaFileObjects("EntityWithQueries"),
            useSuspend = true
        )

        Assert.assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
    }

    @Test
    fun testSuccessWithGenerateAccessorAndSuspendFunctions() {
        val compilation = compileKotlin(
            TestDataHelper.clazzAsJavaFileObjects("EntityWithGenerateAccessor"),
            useSuspend = true
        )

        Assert.assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
    }

    @Test
    fun testSuccessDeprecatedGeneration() {
        val compilation = compileKotlin(
            TestDataHelper.clazzAsJavaFileObjects("EntityWithDeprecatedFields"),
            useSuspend = true
        )
        Assert.assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
    }

    @Test
    fun testSuccessDeprecatedWithReduceGeneration() {
        val compilation = compileKotlin(
            TestDataHelper.clazzAsJavaFileObjects("EntityWithDeprecatedFieldsAndReduce"),
            useSuspend = true
        )
        Assert.assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
    }

    @Test
    fun testSuccessDocIdGeneration() {
        val compilation = compileKotlin(
            TestDataHelper.clazzAsJavaFileObjects("EntityWithDocId"),
            useSuspend = true
        )
        Assert.assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
    }

    @Test
    fun testSuccessDocIdSegmentGeneration() {
        val compilation = compileKotlin(
            TestDataHelper.clazzAsJavaFileObjects("EntityWithDocIdAndDocIdSegments"),
            useSuspend = true
        )
        Assert.assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
    }

    @Test
    fun testFailedWrongDeprecatedGeneration() {
        val compilation = compileKotlin(
            TestDataHelper.clazzAsJavaFileObjects("EntityWithWrongConfiguredDeprecatedFields"),
            useSuspend = true
        )
        Assert.assertEquals(compilation.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
        Assert.assertTrue(compilation.messages.contains("replacement [name2] for field [name] does not exists"))
    }

    @Test
    fun testSuccessDeprecatedClassGeneration() {
        val compilation = compileKotlin(
            TestDataHelper.clazzAsJavaFileObjects("EntityWithDeprecatedClass"),
            useSuspend = true
        )
        Assert.assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
    }

    @Test
    fun testKotlinAbstractGeneration() {
        val subEntity = SourceFile.kotlin(
            "Sub.kt",
            PACKAGE_HEADER +
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

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun testKotlinAbstractGenerationWithLongFields() {
        val subEntity = SourceFile.kotlin(
            "Sub.kt",
            PACKAGE_HEADER +
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
    fun testKotlinSchemaGeneration() {
        val expected = File("src/test/resources/ExpectedSchema.txt").readLines()
        val testObject = SourceFile.kotlin(
            "TestObject.kt",
            PACKAGE_HEADER +
                "import com.schwarz.crystalapi.SchemaClass\n" +
                "@SchemaClass\n" +
                "class TestObject"
        )
        val sub = SourceFile.kotlin(
            "Sub.kt",
            PACKAGE_HEADER +
                "import com.kaufland.testModels.TestObject\n" +
                "import com.schwarz.crystalapi.Field\n" +
                "import com.schwarz.crystalapi.Fields\n" +
                "import com.schwarz.crystalapi.SchemaClass\n" +
                "import java.time.OffsetDateTime\n" +
                "@SchemaClass\n" +
                "@Fields(\n" +
                "Field(name = \"test_test_test\", type = Number::class),\n" +
                "Field(name = \"type\", type = String::class, defaultValue = \"test\", readonly = true),\n" +
                "Field(name = \"list\", type = String::class, list = true),\n" +
                "Field(name = \"someObject\", type = TestObject::class),\n" +
                "Field(name = \"objects\", type = TestObject::class, list = true),\n" +
                "Field(name = \"date_converter_field\", type = OffsetDateTime::class),\n" +
                "Field(name = \"date_converter_list\", type = OffsetDateTime::class, list = true),\n" +
                ")\n" +
                "class Sub"
        )
        val typeConverter = SourceFile.kotlin(
            "DateTypeConverter.kt",
            PACKAGE_HEADER +
                TYPE_CONVERTER_HEADER +
                "import java.time.OffsetDateTime\n" +
                "@TypeConverter\n" +
                "abstract class DateTypeConverter : ITypeConverter<OffsetDateTime, String> {\n" +
                "override fun write(value: OffsetDateTime?): String? = value?.toString()\n" +
                "override fun read(value: String?): OffsetDateTime? = value?.let { OffsetDateTime.parse(it) }\n" +
                "}"
        )
        val compilation = compileKotlin(typeConverter, testObject, sub)

        val actual = compilation.generatedFiles.find { it.name == "SubSchema.kt" }!!.readLines()

        Assert.assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun testKotlinPrivateGeneration() {
        val subEntity = SourceFile.kotlin(
            "Sub.kt",
            PACKAGE_HEADER +
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

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun testKotlinConstructorFailGeneration() {
        val subEntity = SourceFile.kotlin(
            "Sub.kt",
            PACKAGE_HEADER +
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
        Assert.assertTrue(compilation.messages.contains("Entity should not have a constructor"))
    }

    @Test
    fun testTypeConverterGeneration() {
        val expected = File("src/test/resources/ExpectedTypeConverter.txt").readLines()
        val typeConverter = SourceFile.kotlin(
            "DateTypeConverter.kt",
            PACKAGE_HEADER +
                TYPE_CONVERTER_HEADER +
                "import java.time.OffsetDateTime\n" +
                "@TypeConverter\n" +
                "abstract class DateTypeConverter : ITypeConverter<OffsetDateTime, String> {\n" +
                "override fun write(value: OffsetDateTime?): String? = value?.toString()\n" +
                "override fun read(value: String?): OffsetDateTime? = value?.let { OffsetDateTime.parse(it) }\n" +
                "}"
        )

        val compilation = compileKotlin(typeConverter)

        Assert.assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
        val actual = compilation.generatedFiles.find { it.name == "DateTypeConverterInstance.kt" }
            ?.readLines()
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun testTypeConverterFinalClass() {
        val typeConverter = SourceFile.kotlin(
            "DateTypeConverter.kt",
            PACKAGE_HEADER +
                TYPE_CONVERTER_HEADER +
                "import java.time.OffsetDateTime\n" +
                "@TypeConverter\n" +
                "class DateTypeConverter : ITypeConverter<OffsetDateTime, String> {\n" +
                "override fun write(value: OffsetDateTime?): String? = value?.toString()\n" +
                "override fun read(value: String?): OffsetDateTime? = value?.let { OffsetDateTime.parse(it) }\n" +
                "}"
        )

        val compilation = compileKotlin(typeConverter)

        Assert.assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, compilation.exitCode)
        Assert.assertTrue(compilation.messages.contains("TypeConverter can not be final"))
    }

    @Test
    fun testTypeConverterImplementsInterface() {
        val typeConverter = SourceFile.kotlin(
            "DateTypeConverter.kt",
            PACKAGE_HEADER +
                TYPE_CONVERTER_HEADER +
                "import java.time.OffsetDateTime\n" +
                "@TypeConverter\n" +
                "open class DateTypeConverter {\n" +
                "fun write(value: OffsetDateTime?): String? = value?.toString()\n" +
                "fun read(value: String?): OffsetDateTime? = value?.let { OffsetDateTime.parse(it) }\n" +
                "}"
        )

        val compilation = compileKotlin(typeConverter)

        Assert.assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, compilation.exitCode)
        Assert.assertTrue(compilation.messages.contains("Class annotated with ${TypeConverter::class.simpleName} must implement the ${ITypeConverter::class.simpleName} interface"))
    }

    @Test
    fun testTypeConverterExporterGeneration() {
        val expected = File("src/test/resources/ExpectedTypeConverterExporter.txt").readLines().map { it.trim() }
        val sourceFileContents = PACKAGE_HEADER +
            TYPE_CONVERTER_EXPORTER_HEADER +
            TYPE_CONVERTER_HEADER +
            "import java.time.OffsetDateTime\n" +
            "@TypeConverter\n" +
            "abstract class DateTypeConverter : ITypeConverter<OffsetDateTime, String> {\n" +
            "override fun write(value: OffsetDateTime?): String? = value?.toString()\n" +
            "override fun read(value: String?): OffsetDateTime? = value?.let { OffsetDateTime.parse(it) }\n" +
            "}\n" +
            "@TypeConverterExporter\n" +
            "interface TestTypeConverters"
        val typeConverter = SourceFile.kotlin(
            "TestTypeConverters.kt",
            sourceFileContents
        )

        val compilation = compileKotlin(typeConverter)

        Assert.assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
        val actual = compilation.generatedFiles.find { it.name == "TestTypeConvertersInstance.kt" }
            ?.readLines()?.map { it.trim() }
        Assert.assertEquals(expected, actual)
    }

    private fun compileKotlin(
        vararg sourceFiles: SourceFile,
        useSuspend: Boolean = false
    ): JvmCompilationResult {
        return KotlinCompilation().apply {
            sources = sourceFiles.toList()

            // pass your own instance of an annotation processor
            annotationProcessors = listOf(CoachBaseBinderProcessor())
            correctErrorTypes = true
            jvmTarget = "17"

            kaptArgs["useSuspend"] = useSuspend.toString()
            inheritClassPath = true
            messageOutputStream = System.out // see diagnostics in real time
        }.compile()
    }

    companion object {

        const val PACKAGE_HEADER: String =
            "package com.kaufland.testModels\n" +
                "\n"

        const val ENTITY_HEADER: String =
            "import com.schwarz.crystalapi.Entity\n" +
                "import com.schwarz.crystalapi.Field\n" +
                "import com.schwarz.crystalapi.Fields\n"

        const val TYPE_CONVERTER_HEADER: String =
            "import com.schwarz.crystalapi.ITypeConverter\n" +
                "import com.schwarz.crystalapi.TypeConverter\n"

        const val TYPE_CONVERTER_EXPORTER_HEADER: String =
            "import com.schwarz.crystalapi.ITypeConverterExporter\n" +
                "import com.schwarz.crystalapi.TypeConverterExporter\n"

        const val TYPE_CONVERTER_IMPORTER_HEADER: String =
            "package com.kaufland.testModels\n" +
                "\n" +
                "import com.schwarz.crystalapi.ITypeConverterImporter\n" +
                "import com.schwarz.crystalapi.TypeConverterImporter\n"
    }
}
