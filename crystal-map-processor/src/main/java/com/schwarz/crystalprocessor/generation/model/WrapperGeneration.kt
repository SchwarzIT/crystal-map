package com.schwarz.crystalprocessor.generation.model

import com.schwarz.crystalapi.MandatoryCheck
import com.schwarz.crystalprocessor.generation.MapifyableImplGeneration
import com.schwarz.crystalprocessor.model.entity.BaseEntityHolder
import com.schwarz.crystalprocessor.model.entity.WrapperEntityHolder
import com.schwarz.crystalprocessor.util.TypeUtil
import com.squareup.kotlinpoet.*
import java.util.*

class WrapperGeneration {

    fun generateModel(holder: WrapperEntityHolder, useSuspend: Boolean): FileSpec {
        val companionSpec = TypeSpec.companionObjectBuilder()
        companionSpec.superclass(TypeUtil.wrapperCompanion(holder.entityTypeName))

        val builderBuilder = BuilderClassGeneration.generateBaseBuilder(holder)

        val typeBuilder = TypeSpec.classBuilder(holder.entitySimpleName)
            .addSuperinterface(TypeUtil.mapSupport())
            .addModifiers(KModifier.PUBLIC)
            .addSuperinterface(holder.interfaceTypeName)
            .addSuperinterface(MandatoryCheck::class)
            .addFunction(EnsureTypesGeneration.ensureTypes(holder, true))
            .addFunction(CblDefaultGeneration.addDefaults(holder, true))
            .addFunction(CblConstantGeneration.addConstants(holder, true))
            .addFunction(SetAllMethodGeneration().generate(holder, false))
            .addFunction(MapSupportGeneration.toMap(holder))
            .addFunction(ValidateMethodGeneration.generate(holder, false))
            .addProperty(PropertySpec.builder("mDoc", TypeUtil.mutableMapStringAnyNullable()).addModifiers(KModifier.PRIVATE).mutable().initializer("%T()", TypeUtil.linkedHashMapStringAnyNullable()).build())
            .addFunction(constructorMap())
            .addFunction(constructorDefault())
            .superclass(holder.sourceElement.typeName)
            .addFunction(BuilderClassGeneration.generateBuilderFun(holder))

        holder.deprecated?.addDeprecated(typeBuilder)

        holder.docId?.let {
            companionSpec.addFunction(it.companionFunction(holder))
            typeBuilder.addFunction(it.buildExpectedDocId(holder))
            typeBuilder.addSuperinterface(TypeUtil.iDocId())
        }
        if (holder.comment.isNotEmpty()) {
            typeBuilder.addKdoc(KDocGeneration.generate(holder.comment))
        }

        if (holder.modifierOpen) {
            typeBuilder.addModifiers(KModifier.OPEN)
        }

        for (fieldHolder in holder.allFields) {
            companionSpec.addProperties(fieldHolder.createFieldConstant())
            typeBuilder.addProperty(
                fieldHolder.property(
                    dbName = null,
                    collection = null,
                    holder.abstractParts,
                    useMDocChanges = false,
                    holder.deprecated,
                )
            )
            fieldHolder.builderSetter(
                dbName = null,
                collection = null,
                holder.sourcePackage,
                holder.entitySimpleName,
                useMDocChanges = false,
                holder.deprecated
            )?.let {
                builderBuilder.addFunction(it)
            }
        }

        companionSpec.addFunctions(toMap(holder))
        companionSpec.addFunctions(create(holder))
        typeBuilder.addType(companionSpec.build())
        typeBuilder.addFunction(RebindMethodGeneration().generate(false))
        typeBuilder.addType(builderBuilder.build())
        typeBuilder.addType(MapifyableImplGeneration.typeSpec(holder))
        typeBuilder.addAnnotation(MapifyableImplGeneration.impl(holder))

        return FileSpec.get(holder.sourcePackage, typeBuilder.build())
    }

    private fun toMap(holder: BaseEntityHolder): List<FunSpec> {
        val nullCheck = CodeBlock.builder().beginControlFlow("if(obj == null)").addStatement("return mutableMapOf()").endControlFlow().build()

        return Arrays.asList(
            FunSpec.builder("toMap").addModifiers(KModifier.PUBLIC, KModifier.OVERRIDE)
                .addParameter("obj", holder.entityTypeName.copy(nullable = true)).returns(TypeUtil.mutableMapStringAny())
                .addAnnotation(JvmStatic::class)
                .addCode(nullCheck).addStatement("var result = mutableMapOf<%T,%T>()", TypeUtil.string(), TypeUtil.any())
                .beginControlFlow("obj.mDoc.forEach")
                .beginControlFlow("if(it.value != null)").addStatement("result[it.key] = it.value!!").endControlFlow()
                .endControlFlow()
                .addStatement("return result").build()
        )
    }

    private fun constructorMap(): FunSpec {
        return FunSpec.constructorBuilder().addModifiers(KModifier.PUBLIC).addParameter("doc", TypeUtil.mutableMapStringAnyNullable()).addStatement("rebind(ensureTypes(doc))").build()
    }

    private fun constructorDefault(): FunSpec {
        // Add default constructor to allow property-based creators in Jackson
        return FunSpec.constructorBuilder().addModifiers(KModifier.PUBLIC).callThisConstructor("mutableMapOf()").build()
    }

    private fun create(holder: WrapperEntityHolder): List<FunSpec> {
        return Arrays.asList(
            FunSpec.builder("create").addModifiers(KModifier.PUBLIC, KModifier.OVERRIDE).addParameter("doc", TypeUtil.mutableMapStringAnyNullable()).addAnnotation(JvmStatic::class).addStatement(
                "return %N(doc)",
                holder.entitySimpleName
            ).returns(holder.entityTypeName).build(),
            FunSpec.builder("create").addModifiers(KModifier.PUBLIC, KModifier.OVERRIDE).addAnnotation(JvmStatic::class).addStatement(
                "return %N(%T())",
                holder.entitySimpleName,
                TypeUtil.hashMapStringAnyNullable()
            ).returns(holder.entityTypeName).build()
        )
    }
}
