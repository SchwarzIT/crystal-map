package com.schwarz.crystalksp

import com.schwarz.crystalapi.ITypeConverter
import com.schwarz.crystalapi.TypeConverter
import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.configureKsp
import com.tschuchort.compiletesting.kspProcessorOptions
import com.tschuchort.compiletesting.sourcesGeneratedBySymbolProcessor
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCompilerApi::class)
class CrystalProcessorTest {
    @Test
    fun testSuccessSimpleMapper() {
        val compilation = compileKotlin(TestDataHelper.clazzAsJavaFileObjects("SimpleMapperTest"))

        assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
    }

    @Test
    fun testSuccessSimpleReduce() {
        val compilation =
            compileKotlin(TestDataHelper.clazzAsJavaFileObjects("EntityWithSimpleReduce"))

        assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
    }

    @Test
    fun testSuccessMapperWithGetterAndSetter() {
        val compilation =
            compileKotlin(TestDataHelper.clazzAsJavaFileObjects("MapperWithGetterAndSetter"))

        assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
    }

    @Test
    fun testSuccessMapperWithTypeParam() {
        val compilation =
            compileKotlin(TestDataHelper.clazzAsJavaFileObjects("MapperWithTypeParam"))

        assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
    }

    @Test
    fun testSuccessMapperWithNullable() {
        val compilation = compileKotlin(TestDataHelper.clazzAsJavaFileObjects("MapperWithNullable"))

        assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
    }

    @Test
    fun testSuccessWithQueries() {
        val compilation = compileKotlin(TestDataHelper.clazzAsJavaFileObjects("EntityWithQueries"))

        assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
    }

    @Test
    fun testSucessWithQueriesAndEnums() {
        val compilation =
            compileKotlin(TestDataHelper.clazzAsJavaFileObjects("EntityWithQueriesAndEnums"))

        assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
    }

    @Test
    fun testSuccessWithGenerateAccessor() {
        val compilation =
            compileKotlin(TestDataHelper.clazzAsJavaFileObjects("EntityWithGenerateAccessor"))

        assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
    }

    @Test
    fun testSuccessWithQueriesAndSuspendFunctions() {
        val compilation =
            compileKotlin(
                TestDataHelper.clazzAsJavaFileObjects("EntityWithQueries"),
                useSuspend = true,
            )

        assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
    }

    @Test
    fun testSuccessWithGenerateAccessorAndSuspendFunctions() {
        val compilation =
            compileKotlin(
                TestDataHelper.clazzAsJavaFileObjects("EntityWithGenerateAccessor"),
                useSuspend = true,
            )

        assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
    }

    @Test
    fun testSuccessDeprecatedGeneration() {
        val compilation =
            compileKotlin(
                TestDataHelper.clazzAsJavaFileObjects("EntityWithDeprecatedFields"),
                useSuspend = true,
            )
        assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
    }

    @Test
    fun testSuccessDeprecatedWithReduceGeneration() {
        val compilation =
            compileKotlin(
                TestDataHelper.clazzAsJavaFileObjects("EntityWithDeprecatedFieldsAndReduce"),
                useSuspend = true,
            )
        assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
    }

    @Test
    fun testSuccessDocIdGeneration() {
        val compilation =
            compileKotlin(
                TestDataHelper.clazzAsJavaFileObjects("EntityWithDocId"),
                useSuspend = true,
            )
        assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
    }

    @Test
    fun testSuccessDocIdEnumGeneration() {
        val compilation =
            compileKotlin(
                TestDataHelper.clazzAsJavaFileObjects("EntityWithEnumDocId"),
                useSuspend = true,
            )
        assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
    }

    @Test
    fun testSuccessDocIdSegmentGeneration() {
        val compilation =
            compileKotlin(
                TestDataHelper.clazzAsJavaFileObjects("EntityWithDocIdAndDocIdSegments"),
                useSuspend = true,
            )
        assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
    }

    @Test
    fun testFailedWrongDeprecatedGeneration() {
        val compilation =
            compileKotlin(
                TestDataHelper.clazzAsJavaFileObjects("EntityWithWrongConfiguredDeprecatedFields"),
                useSuspend = true,
            )
        assertEquals(compilation.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertTrue(
            compilation.messages.contains("replacement [name2] for field [name] does not exists"),
        )
    }

    @Test
    fun testSuccessDeprecatedClassGeneration() {
        val compilation =
            compileKotlin(
                TestDataHelper.clazzAsJavaFileObjects("EntityWithDeprecatedClass"),
                useSuspend = true,
            )
        assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
    }

    @Test
    fun testKotlinAbstractGeneration() {
        val subEntity =
            SourceFile.kotlin(
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
                    "}",
            )

        val compilation = compileKotlin(subEntity)

        assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun testKotlinAbstractGenerationWithLongFields() {
        val subEntity =
            SourceFile.kotlin(
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
                    "}",
            )

        val compilation = compileKotlin(subEntity)

        assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
    }

    @Test
    fun testKotlinSchemaGeneration() {
        val expected =
            String(
                this::class.java.classLoader
                    .getResourceAsStream("ExpectedSchema.txt")
                    .readAllBytes(),
            ).lines()
        val testObject =
            SourceFile.kotlin(
                "TestObject.kt",
                PACKAGE_HEADER +
                    "import com.schwarz.crystalapi.SchemaClass\n" +
                    "@SchemaClass\n" +
                    "class TestObject",
            )
        val sub =
            SourceFile.kotlin(
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
                    "class Sub",
            )
        val typeConverter =
            SourceFile.kotlin(
                "DateTypeConverter.kt",
                PACKAGE_HEADER +
                    TYPE_CONVERTER_HEADER +
                    "import java.time.OffsetDateTime\n" +
                    "@TypeConverter\n" +
                    "abstract class DateTypeConverter : ITypeConverter<OffsetDateTime, String> {\n" +
                    "override fun write(value: OffsetDateTime?): String? = value?.toString()\n" +
                    "override fun read(value: String?): OffsetDateTime? = value?.let { OffsetDateTime.parse(it) }\n" +
                    "}",
            )
        val compilation = compileKotlin(typeConverter, testObject, sub)

        val actual =
            compilation.sourcesGeneratedBySymbolProcessor
                .find {
                    it.name == "SubSchema.kt"
                }!!
                .readLines()

        assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
        assertEquals(expected, actual)
    }

    @Test
    fun testKotlinSchemaGenerationWithBasedOn() {
        val expected =
            String(
                this::class.java.classLoader
                    .getResourceAsStream("ExpectedSubSchema.txt")
                    .readAllBytes(),
            ).lines()
        val testObject =
            SourceFile.kotlin(
                "TestObject.kt",
                PACKAGE_HEADER +
                    "import com.schwarz.crystalapi.Fields\n" +
                    "import com.schwarz.crystalapi.Field\n" +
                    "import com.schwarz.crystalapi.MapWrapper\n" +
                    "import com.schwarz.crystalapi.SchemaClass\n" +
                    "@MapWrapper\n" +
                    "@SchemaClass\n" +
                    "@Fields(\n" +
                    "Field(name = \"type\", type = String::class, defaultValue = \"test\", readonly = true)\n" +
                    ")\n" +
                    "open class TestObject",
            )
        val base =
            SourceFile.kotlin(
                "Base.kt",
                PACKAGE_HEADER +
                    "import com.schwarz.crystalapi.BaseModel\n" +
                    "import com.schwarz.crystalapi.Fields\n" +
                    "import com.schwarz.crystalapi.Field\n" +
                    "@BaseModel\n" +
                    "@Fields(\n" +
                    "Field(name = \"someObject\", type = TestObject::class)\n" +
                    ")\n" +
                    "open class Base",
            )
        val sub =
            SourceFile.kotlin(
                "Sub.kt",
                PACKAGE_HEADER +
                    "import com.kaufland.testModels.TestObject\n" +
                    "import com.schwarz.crystalapi.BasedOn\n" +
                    "import com.schwarz.crystalapi.Field\n" +
                    "import com.schwarz.crystalapi.Fields\n" +
                    "import com.schwarz.crystalapi.SchemaClass\n" +
                    "import java.time.OffsetDateTime\n" +
                    "@SchemaClass\n" +
                    "@BasedOn(Base::class)\n" +
                    "@Fields(\n" +
                    "Field(name = \"type\", type = String::class, defaultValue = \"sub\", readonly = true)\n" +
                    ")\n" +
                    "class Sub",
            )
        val compilation = compileKotlin(base, testObject, sub)

        val actual =
            compilation.sourcesGeneratedBySymbolProcessor
                .find {
                    it.name == "SubSchema.kt"
                }!!
                .readLines()

        assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
        assertEquals(expected, actual)
    }

    @Test
    fun testKotlinPrivateGeneration() {
        val subEntity =
            SourceFile.kotlin(
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
                    "}",
            )

        val compilation = compileKotlin(subEntity)

        assertEquals(compilation.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertTrue(compilation.messages.contains("Entity can not be final"))
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun testKotlinConstructorFailGeneration() {
        val subEntity =
            SourceFile.kotlin(
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
                    "}",
            )

        val compilation = compileKotlin(subEntity)

        assertEquals(compilation.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertTrue(compilation.messages.contains("Entity should not have a constructor"))
    }

    @Test
    fun testTypeConverterGeneration() {
        val expected =
            String(
                this::class.java.classLoader
                    .getResourceAsStream(
                        "ExpectedTypeConverter.txt",
                    ).readAllBytes(),
            ).lines()

        val typeConverter =
            SourceFile.kotlin(
                "DateTypeConverter.kt",
                PACKAGE_HEADER +
                    TYPE_CONVERTER_HEADER +
                    "import java.time.OffsetDateTime\n" +
                    "@TypeConverter\n" +
                    "abstract class DateTypeConverter : ITypeConverter<OffsetDateTime, String> {\n" +
                    "override fun write(value: OffsetDateTime?): String? = value?.toString()\n" +
                    "override fun read(value: String?): OffsetDateTime? = value?.let { OffsetDateTime.parse(it) }\n" +
                    "}",
            )

        val compilation = compileKotlin(typeConverter)

        assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
        val actual =
            compilation.sourcesGeneratedBySymbolProcessor
                .find {
                    it.name ==
                        "DateTypeConverterInstance.kt"
                }?.readLines()
        assertEquals(expected, actual)
    }

    @Test
    fun testTypeConverterFinalClass() {
        val typeConverter =
            SourceFile.kotlin(
                "DateTypeConverter.kt",
                PACKAGE_HEADER +
                    TYPE_CONVERTER_HEADER +
                    "import java.time.OffsetDateTime\n" +
                    "@TypeConverter\n" +
                    "class DateTypeConverter : ITypeConverter<OffsetDateTime, String> {\n" +
                    "override fun write(value: OffsetDateTime?): String? = value?.toString()\n" +
                    "override fun read(value: String?): OffsetDateTime? = value?.let { OffsetDateTime.parse(it) }\n" +
                    "}",
            )

        val compilation = compileKotlin(typeConverter)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, compilation.exitCode)
        assertTrue(compilation.messages.contains("TypeConverter can not be final"))
    }

    @Test
    fun testTypeConverterImplementsInterface() {
        val typeConverter =
            SourceFile.kotlin(
                "DateTypeConverter.kt",
                PACKAGE_HEADER +
                    TYPE_CONVERTER_HEADER +
                    "import java.time.OffsetDateTime\n" +
                    "@TypeConverter\n" +
                    "open class DateTypeConverter {\n" +
                    "fun write(value: OffsetDateTime?): String? = value?.toString()\n" +
                    "fun read(value: String?): OffsetDateTime? = value?.let { OffsetDateTime.parse(it) }\n" +
                    "}",
            )

        val compilation = compileKotlin(typeConverter)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, compilation.exitCode)
        assertTrue(
            compilation.messages.contains(
                "Class annotated with ${TypeConverter::class.simpleName} must implement the ${ITypeConverter::class.simpleName} interface",
            ),
        )
    }

    @Test
    fun testTypeConverterExporterGeneration() {
        val expected =
            String(
                this::class.java.classLoader
                    .getResourceAsStream(
                        "ExpectedTypeConverterExporter.txt",
                    ).readAllBytes(),
            ).lines().map {
                it.trim()
            }

        val sourceFileContents =
            PACKAGE_HEADER +
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
        val typeConverter =
            SourceFile.kotlin(
                "TestTypeConverters.kt",
                sourceFileContents,
            )

        val compilation = compileKotlin(typeConverter)

        assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
        val actual =
            compilation.sourcesGeneratedBySymbolProcessor
                .find {
                    it.name ==
                        "TestTypeConvertersInstance.kt"
                }?.readLines()
                ?.map { it.trim() }
        assertEquals(expected, actual)
    }

    @Test
    fun testTypeConverterExporterGenerationWithGenericTypes() {
        val expected =
            String(
                this::class.java.classLoader
                    .getResourceAsStream(
                        "ExpectedTypeConverterExporterGenerics.txt",
                    ).readAllBytes(),
            ).lines().map {
                it.trim()
            }

        val sourceFileContents =
            PACKAGE_HEADER +
                TYPE_CONVERTER_EXPORTER_HEADER +
                TYPE_CONVERTER_HEADER +
                "import java.time.OffsetDateTime\n" +
                "@TypeConverter\n" +
                "abstract class DateTypeConverter : ITypeConverter<OffsetDateTime, Map<String, Any?>> {\n" +
                "override fun write(value: OffsetDateTime?): Map<String, Any?>? = mapOf()\n" +
                "override fun read(value: Map<String, Any?>?): OffsetDateTime? = OffsetDateTime.now()\n" +
                "}\n" +
                "@TypeConverterExporter\n" +
                "interface TestTypeConvertersGeneric"
        val typeConverter =
            SourceFile.kotlin(
                "TestTypeConvertersGeneric.kt",
                sourceFileContents,
            )

        val compilation = compileKotlin(typeConverter)

        assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
        val actual =
            compilation.sourcesGeneratedBySymbolProcessor
                .find {
                    it.name ==
                        "TestTypeConvertersGenericInstance.kt"
                }?.readLines()
                ?.map { it.trim() }

        assertEquals(expected, actual)
    }

    // ===== Phase 0: Cross-Entity Field References (0A) =====

    @Test
    fun testCrossEntityFieldReference() {
        val compilation =
            compileKotlin(
                TestDataHelper.clazzAsJavaFileObjects("CrossRefWrapper"),
                TestDataHelper.clazzAsJavaFileObjects("CrossRefEntity"),
            )
        assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
    }

    // ===== Phase 0: @BasedOn Inheritance Chains (0B) =====

    @Test
    fun testBasedOnSingleLevel() {
        val compilation =
            compileKotlin(
                TestDataHelper.clazzAsJavaFileObjects("BaseModelSingle"),
                TestDataHelper.clazzAsJavaFileObjects("EntityWithBasedOnSingle"),
            )
        assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
        // Verify generated entity contains fields from parent BaseModel
        val generatedEntity =
            compilation.sourcesGeneratedBySymbolProcessor
                .find { it.name == "EntityWithBasedOnSingleEntity.kt" }
        assertTrue(generatedEntity != null, "EntityWithBasedOnSingleEntity should be generated")
        val content = generatedEntity!!.readText()
        assertTrue(content.contains("base_field"), "Generated entity should contain base_field from parent")
        assertTrue(content.contains("own_field"), "Generated entity should contain own_field")
    }

    @Test
    fun testBasedOnMultiLevelChain() {
        val compilation =
            compileKotlin(
                TestDataHelper.clazzAsJavaFileObjects("BaseModelSingle"),
                TestDataHelper.clazzAsJavaFileObjects("BaseModelChained"),
                TestDataHelper.clazzAsJavaFileObjects("EntityWithChainedBasedOn"),
            )
        assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
        val generatedEntity =
            compilation.sourcesGeneratedBySymbolProcessor
                .find { it.name == "EntityWithChainedBasedOnEntity.kt" }
        assertTrue(generatedEntity != null, "EntityWithChainedBasedOnEntity should be generated")
        val content = generatedEntity!!.readText()
        assertTrue(content.contains("base_field"), "Should contain base_field from grandparent")
        assertTrue(content.contains("base_number"), "Should contain base_number from grandparent")
        assertTrue(content.contains("chained_field"), "Should contain chained_field from parent")
        assertTrue(content.contains("leaf_field"), "Should contain leaf_field from entity itself")
    }

    // ===== Phase 0: TypeConverter Validation (0C) =====

    @Test
    fun testEntityWithCustomTypeWithoutTypeConverter() {
        val customType =
            SourceFile.kotlin(
                "CustomType.kt",
                PACKAGE_HEADER +
                    "class CustomType(val value: String)\n",
            )
        val entity =
            SourceFile.kotlin(
                "EntityWithCustomType.kt",
                PACKAGE_HEADER +
                    ENTITY_HEADER +
                    "@Entity(database = \"test_db\")\n" +
                    "@Fields(\n" +
                    "Field(name = \"custom\", type = CustomType::class),\n" +
                    "Field(name = \"type\", type = String::class, defaultValue = \"test\", readonly = true)\n" +
                    ")\n" +
                    "open class EntityWithCustomType\n",
            )
        val compilation = compileKotlin(customType, entity)
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, compilation.exitCode)
        assertTrue(
            compilation.messages.contains("TypeConverter"),
            "Error should mention TypeConverter: ${compilation.messages}",
        )
    }

    // ===== Phase 0: Deprecated Cross-Entity Validation (0D) =====

    @Test
    fun testDeprecatedEntityValid() {
        val compilation =
            compileKotlin(
                TestDataHelper.clazzAsJavaFileObjects("DeprecatedEntityWithValidReplacedBy"),
            )
        assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
    }

    // ===== Phase 6: Cross-Entity Accessor Type Resolution (6A/6B) =====

    @Test
    fun testCrossEntityAccessorReturnType() {
        val compilation =
            compileKotlin(
                TestDataHelper.clazzAsJavaFileObjects("EntityWithCrossAccessorA"),
                TestDataHelper.clazzAsJavaFileObjects("EntityWithCrossAccessorB"),
            )
        assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
    }

    @Test
    fun testCircularCrossEntityAccessors() {
        val compilation =
            compileKotlin(
                TestDataHelper.clazzAsJavaFileObjects("CircularRefEntityA"),
                TestDataHelper.clazzAsJavaFileObjects("CircularRefEntityB"),
            )
        assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
    }

    // ===== Phase 0: Multiple Entities with @Reduce (0E) =====

    @Test
    fun testMultipleReduces() {
        val compilation =
            compileKotlin(
                TestDataHelper.clazzAsJavaFileObjects("EntityWithTwoReduces"),
            )
        assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)
        val generatedFiles =
            compilation.sourcesGeneratedBySymbolProcessor.map { it.name }
        assertTrue(
            generatedFiles.any { it.contains("Small") },
            "Should generate Small reduced entity: $generatedFiles",
        )
        assertTrue(
            generatedFiles.any { it.contains("Medium") },
            "Should generate Medium reduced entity: $generatedFiles",
        )
    }

    @OptIn(ExperimentalCompilerApi::class)
    private fun compileKotlin(
        vararg sourceFiles: SourceFile,
        useSuspend: Boolean = false,
    ): JvmCompilationResult =
        KotlinCompilation()
            .apply {
                configureKsp {
                    symbolProcessorProviders.add(CrystalProcessorProvider())
                }
                languageVersion = "2.1"
                sources = sourceFiles.toMutableList()
                jvmTarget = "17"
                kspProcessorOptions["useSuspend"] = useSuspend.toString()
                inheritClassPath = true
                // messageOutputStream = System.out // see diagnostics in real time
            }.compile()

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
