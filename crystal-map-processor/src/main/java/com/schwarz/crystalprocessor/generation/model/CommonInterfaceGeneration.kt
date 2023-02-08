package com.schwarz.crystalprocessor.generation.model

import com.schwarz.crystalprocessor.model.entity.BaseEntityHolder
import com.schwarz.crystalprocessor.model.entity.BaseModelHolder
import com.schwarz.crystalprocessor.util.TypeUtil
import com.squareup.kotlinpoet.*
import java.util.*

private const val GENERATED_REPRESENT_NAME = "Represent"
class CommonInterfaceGeneration {

    fun generateModel(holder: BaseEntityHolder, useSuspend: Boolean): FileSpec {

        val interfaceSpec = TypeSpec.interfaceBuilder(holder.interfaceSimpleName)
        interfaceSpec.addSuperinterface(TypeUtil.mapSupport())
        holder.basedOn.forEach { interfaceSpec.addSuperinterface(it.interfaceTypeName) }

        val companionSpec = TypeSpec.companionObjectBuilder()

        for (fieldHolder in holder.allFields) {
            val isBaseField = holder.basedOn.any {
                it.fields.containsKey(fieldHolder.dbField) || it.fieldConstants.containsKey(fieldHolder.dbField)
            }
            val propertySpec = fieldHolder.interfaceProperty(isBaseField)
            interfaceSpec.addProperty(propertySpec)

            companionSpec.addProperties(fieldHolder.createFieldConstant())
        }

        if (holder is BaseModelHolder) {
            companionSpec.addFunctions(fromMap(holder))
            generateRepresent(holder, interfaceSpec, useSuspend)
        }
        interfaceSpec.addType(companionSpec.build())

        return FileSpec.get(holder.sourcePackage, interfaceSpec.build())
    }

    private fun generateRepresent(holder: BaseModelHolder, parent: TypeSpec.Builder, useSuspend: Boolean) {

        val typeBuilder = TypeSpec.classBuilder(GENERATED_REPRESENT_NAME)
            .addSuperinterface(TypeUtil.mapSupport())
            .addModifiers(KModifier.PRIVATE)
            .addSuperinterface(holder.interfaceTypeName)
            .addFunction(EnsureTypesGeneration.ensureTypes(holder, true))
            .addFunction(CblConstantGeneration.addConstants(holder, true))
            .addFunction(SetAllMethodGeneration().generate(holder, false))
            .addFunction(MapSupportGeneration.toMap(holder))
            .addProperty(
                PropertySpec.builder("mDoc", TypeUtil.mutableMapStringAnyNullable()).addModifiers(
                    KModifier.PRIVATE
                ).mutable().initializer("%T()", TypeUtil.linkedHashMapStringAnyNullable()).build()
            )
            .addFunction(constructorMap())
            .superclass(holder.sourceElement!!.typeName)

        holder.deprecated?.addDeprecated(typeBuilder)

        if (holder.comment.isNotEmpty()) {
            typeBuilder.addKdoc(KDocGeneration.generate(holder.comment))
        }

        for (fieldHolder in holder.allFields) {
            typeBuilder.addProperty(fieldHolder.property(null, holder.abstractParts, false, holder.deprecated))
        }

        val companionSpec = TypeSpec.companionObjectBuilder()
        companionSpec.addFunctions(fromMapRepresent(holder))
        companionSpec.addFunctions(toMapRepresent(holder))
        companionSpec.addFunctions(TypeConversionMethodsGeneration(useSuspend).generate())

        typeBuilder.addType(companionSpec.build())

        parent.addType(typeBuilder.build())
    }

    private fun constructorMap(): FunSpec {
        return FunSpec.constructorBuilder().addModifiers(KModifier.PUBLIC).addParameter("doc", TypeUtil.mutableMapStringAnyNullable()).addStatement("mDoc = ensureTypes(doc).toMutableMap()").build()
    }

    private fun toMapRepresent(holder: BaseModelHolder): List<FunSpec> {
        val nullCheck = CodeBlock.builder().beginControlFlow("if(obj == null)").addStatement("return mutableMapOf()").endControlFlow().build()
        val nullCheckList = CodeBlock.builder().beginControlFlow("if(obj == null)").addStatement("return listOf()").endControlFlow().build()

        return Arrays.asList(
            FunSpec.builder("toMap").addModifiers(KModifier.PUBLIC)
                .addParameter("obj", holder.representTypeName.copy(nullable = true)).returns(TypeUtil.mutableMapStringAny())
                .addAnnotation(JvmStatic::class)
                .addCode(nullCheck).addStatement("var result = mutableMapOf<%T,%T>()", TypeUtil.string(), TypeUtil.any())
                .beginControlFlow("obj.mDoc.forEach")
                .beginControlFlow("if(it.value != null)").addStatement("result[it.key] = it.value!!").endControlFlow()
                .endControlFlow()
                .addStatement("return result").build(),

            FunSpec.builder("toMap").addModifiers(KModifier.PUBLIC)
                .addParameter("obj", TypeUtil.list(holder.representTypeName).copy(nullable = true)).addAnnotation(JvmStatic::class)
                .returns(TypeUtil.listWithMutableMapStringAny()).addCode(nullCheckList)
                .addStatement("var result = %T()", TypeUtil.arrayListWithMutableMapStringAny())
                .addCode(
                    CodeBlock.builder()
                        .beginControlFlow("for(entry in obj)")
                        .addStatement("var temp = mutableMapOf<%T,%T>()", TypeUtil.string(), TypeUtil.any())
                        .addStatement("temp.putAll(%N.toMap(entry)!!)", GENERATED_REPRESENT_NAME)
                        .addStatement("result.add(temp)").endControlFlow().build()
                )
                .addStatement("return result").build()
        )
    }

    private fun fromMapRepresent(holder: BaseModelHolder): List<FunSpec> {
        val nullCheck = CodeBlock.builder().beginControlFlow("if(obj == null)").addStatement("return null").endControlFlow().build()

        return Arrays.asList(
            FunSpec.builder("fromMap").addModifiers(KModifier.PUBLIC)
                .addParameter("obj", TypeUtil.mutableMapStringAnyNullable().copy(nullable = true)).addAnnotation(JvmStatic::class)
                .returns(holder.representTypeName.copy(nullable = true)).addCode(nullCheck)
                .addStatement("return %T(obj)", holder.representTypeName).build(),

            FunSpec.builder("fromMap").addModifiers(KModifier.PUBLIC).addAnnotation(JvmStatic::class)
                .addParameter("obj", TypeUtil.listWithMutableMapStringAnyNullable().copy(nullable = true))
                .returns(TypeUtil.list(holder.representTypeName).copy(nullable = true)).addCode(nullCheck)
                .addStatement("var result = %T()", TypeUtil.arrayList(holder.representTypeName))
                .addCode(
                    CodeBlock.builder().beginControlFlow("for(entry in obj)")
                        .addStatement("result.add(%N(entry))", GENERATED_REPRESENT_NAME)
                        .endControlFlow().build()
                ).addStatement("return result").build()
        )
    }

    private fun fromMap(holder: BaseModelHolder): List<FunSpec> {
        val nullCheck = CodeBlock.builder().beginControlFlow("if(obj == null)").addStatement("return null").endControlFlow().build()

        return Arrays.asList(
            FunSpec.builder("fromMap").addModifiers(KModifier.PUBLIC)
                .addParameter("obj", TypeUtil.mutableMapStringAnyNullable().copy(nullable = true)).addAnnotation(JvmStatic::class)
                .returns(holder.interfaceTypeName.copy(nullable = true)).addCode(nullCheck)
                .addStatement("return %N.fromMap(obj)", GENERATED_REPRESENT_NAME).build(),

            FunSpec.builder("fromMap").addModifiers(KModifier.PUBLIC).addAnnotation(JvmStatic::class)
                .addParameter("obj", TypeUtil.listWithMutableMapStringAnyNullable().copy(nullable = true))
                .returns(TypeUtil.list(holder.interfaceTypeName).copy(nullable = true)).addCode(nullCheck)
                .addStatement("return %N.fromMap(obj)", GENERATED_REPRESENT_NAME).build()
        )
    }
}
