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

        typeConverterHolders.forEach {
            codeBlockBuilder.add(
                "%T(\n%T(%S, %S), \n%T(%S, %S), \n%T(%S, %S)\n),\n",
                TypeConverterImportable::class,
                ClassNameDefinition::class,
                it.instanceClassTypeName.packageName,
                it.instanceClassTypeName.simpleName,
                ClassNameDefinition::class,
                it.domainClassTypeName.packageName,
                it.domainClassTypeName.simpleName,
                ClassNameDefinition::class,
                it.mapClassTypeName.packageName,
                it.mapClassTypeName.simpleName
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
