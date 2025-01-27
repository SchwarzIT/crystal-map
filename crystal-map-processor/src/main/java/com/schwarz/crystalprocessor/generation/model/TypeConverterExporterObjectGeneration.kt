package com.schwarz.crystalprocessor.generation.model

import com.schwarz.crystalapi.ClassNameDefinition
import com.schwarz.crystalapi.ITypeConverterExporter
import com.schwarz.crystalapi.TypeConverterImportable
import com.schwarz.crystalprocessor.model.typeconverter.TypeConverterExporterHolder
import com.schwarz.crystalprocessor.model.typeconverter.TypeConverterHolder
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeSpec

object TypeConverterExporterObjectGeneration {

    val map: Map<String, String> = mapOf()

    fun generateTypeConverterExporterObject(
        typeConverterExporterHolder: TypeConverterExporterHolder,
        typeConverterHolders: List<TypeConverterHolder>
    ): FileSpec {
        val typeSpec = TypeSpec.classBuilder(typeConverterExporterHolder.name + "Instance")
            .addSuperinterface(
                ClassName(
                    typeConverterExporterHolder.sourcePackageName,
                    typeConverterExporterHolder.name
                )
            )
            .addSuperinterface(ITypeConverterExporter::class)
            .addProperty(
                getTypeConvertersSpec(typeConverterHolders)
            )
            .addProperty(
                getTypeConverterImportablesSpec(typeConverterHolders)
            )
            .build()

        return FileSpec.get(typeConverterExporterHolder.sourcePackageName, typeSpec)
    }

    private fun getTypeConvertersSpec(typeConverterHolders: List<TypeConverterHolder>): PropertySpec {
        val codeBlockBuilder = CodeBlock.Builder()

        codeBlockBuilder.add("return mapOf(\n")

        typeConverterHolders.forEach {
            codeBlockBuilder.add(
                "%T::class to %T,\n",
                it.domainClassTypeName,
                it.instanceClassTypeName
            )
        }

        codeBlockBuilder.add(")")

        val codeBlock = codeBlockBuilder.build()

        return PropertySpec.builder("typeConverters", typeConverterMapType())
            .getter(
                FunSpec.getterBuilder()
                    .addCode(codeBlock)
                    .build()
            )
            .addModifiers(KModifier.OVERRIDE)
            .build()
    }

    private fun getTypeConverterImportablesSpec(typeConverterHolders: List<TypeConverterHolder>): PropertySpec {
        val codeBlockBuilder = CodeBlock.Builder()

        codeBlockBuilder.add("return listOf(\n")

        typeConverterHolders.forEach { typeConverterHolder ->
            val genericTypeNames = typeConverterHolder.genericTypeNames.toKotlinCodeString()
            codeBlockBuilder.add(
                "%T(\n  %T(%S, %S), \n  %T(%S, %S), \n  %T(%S, %S),\n  ${genericTypeNames}\n),\n",
                TypeConverterImportable::class,
                ClassNameDefinition::class,
                typeConverterHolder.instanceClassTypeName.packageName,
                typeConverterHolder.instanceClassTypeName.simpleName,
                ClassNameDefinition::class,
                typeConverterHolder.domainClassTypeName.packageName,
                typeConverterHolder.domainClassTypeName.simpleName,
                ClassNameDefinition::class,
                typeConverterHolder.mapClassTypeName.packageName,
                typeConverterHolder.mapClassTypeName.simpleName
            )
        }

        codeBlockBuilder.add(")")

        val codeBlock = codeBlockBuilder.build()

        return PropertySpec.builder("typeConverterImportables", typeConverterImportablesListType())
            .getter(
                FunSpec.getterBuilder()
                    .addCode(codeBlock)
                    .build()
            )
            .addModifiers(KModifier.OVERRIDE)
            .build()
    }

    /**
     * Returns a Kotlin code string like:
     *
     * listOf(
     *     ClassNameDefinition("kotlin", "String"),
     *     ClassNameDefinition("kotlin.collections", "Map", listOf(
     *         ClassNameDefinition("kotlin", "String"),
     *         ClassNameDefinition("kotlin", "Any", nullable = true),
     *     ))
     * )
     */
    private fun List<ClassNameDefinition>.toKotlinCodeString(
        indentLevel: Int = 1
    ): String {
        if (isEmpty()) return "listOf()"

        val indent = "  ".repeat(indentLevel)
        val childIndent = "  ".repeat(indentLevel + 1)

        return buildString {
            append("generics = listOf(\n")
            this@toKotlinCodeString.forEach { definition ->
                append(childIndent)
                append(definition.toKotlinCodeString(indentLevel + 1))
                append(",\n")
            }
            append(indent).append(")")
        }
    }

    private fun ClassNameDefinition.toKotlinCodeString(
        indentLevel: Int
    ): String {
        val indent = "  ".repeat(indentLevel)

        val nullableString = if (nullable) ", nullable = true" else ""
        if (generics.isNullOrEmpty()) {
            return """ClassNameDefinition("$packageName", "$className"$nullableString)"""
        }

        return buildString {
            append("""ClassNameDefinition("$packageName", "$className"$nullableString, """)
            append("\n")
            append(indent)
            append(generics?.toKotlinCodeString(indentLevel + 1))
            append("\n")
            append(indent).append(")")
        }
    }

    private fun typeConverterMapType() = ClassName("kotlin.collections", "Map")
        .parameterizedBy(
            ClassName("kotlin.reflect", "KClass").parameterizedBy(STAR),
            ClassName("com.schwarz.crystalapi", "ITypeConverter").parameterizedBy(STAR, STAR)
        )

    private fun typeConverterImportablesListType() = ClassName("kotlin.collections", "List")
        .parameterizedBy(
            ClassName("com.schwarz.crystalapi", "TypeConverterImportable")
        )
}
