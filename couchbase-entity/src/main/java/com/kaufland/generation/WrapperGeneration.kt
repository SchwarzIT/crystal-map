package com.kaufland.generation

import com.kaufland.model.entity.BaseEntityHolder
import com.kaufland.model.entity.WrapperEntityHolder
import com.kaufland.util.TypeUtil
import com.squareup.kotlinpoet.*
import java.util.*

class WrapperGeneration {

    fun generateModel(holder: WrapperEntityHolder, useSuspend: Boolean): FileSpec {

        val companionSpec = TypeSpec.companionObjectBuilder()

        val builderBuilder = BuilderClassGeneration.generateBaseBuilder(holder)

        val typeBuilder = TypeSpec.classBuilder(holder.entitySimpleName)
                .addSuperinterface(TypeUtil.mapSupport())
                .addModifiers(KModifier.PUBLIC)
                .addSuperinterface(holder.interfaceTypeName)
                .addFunction(EnsureTypesGeneration.ensureTypes(holder, true))
                .addFunction(CblDefaultGeneration.addDefaults(holder, true))
                .addFunction(CblConstantGeneration.addConstants(holder, true))
                .addFunction(MapSupportGeneration.toMap(holder))
                .addProperty(PropertySpec.builder("mDoc", TypeUtil.mutableMapStringAnyNullable()).addModifiers(KModifier.PRIVATE).mutable().initializer("%T()", TypeUtil.linkedHashMapStringAnyNullable()).build())
                .addFunction(constructorMap())
                .addFunction(constructorDefault())
                .superclass(holder.sourceElement!!.asType().asTypeName())
                .addFunction(BuilderClassGeneration.generateBuilderFun())

        holder.deprecated?.addDeprecated(typeBuilder)

        holder.docId?.let {
            companionSpec.addFunction(it.companionFunction(holder))
            typeBuilder.addFunction(it.buildExpectedDocId(holder))
            typeBuilder.addSuperinterface(TypeUtil.iDocId())
        }
        if (holder.comment.isNotEmpty()) {
            typeBuilder.addKdoc(holder.comment.joinToString(separator = "\n"))
        }

        for (baseModelHolder in holder.basedOn) {
            typeBuilder.addSuperinterface(baseModelHolder.interfaceTypeName)
        }

        if(holder.modifierOpen){
            typeBuilder.addModifiers(KModifier.OPEN)
        }

        for (fieldHolder in holder.allFields) {

            companionSpec.addProperties(fieldHolder.createFieldConstant())
            typeBuilder.addProperty(fieldHolder.property(null, holder.abstractParts, false, holder.deprecated))
            fieldHolder.builderSetter(null, holder.`package`, holder.entitySimpleName, false)?.let {
                builderBuilder.addFunction(it)
            }
        }

        companionSpec.addFunctions(fromMap(holder))
        companionSpec.addFunctions(toMap(holder))
        companionSpec.addFunctions(create(holder))
        typeBuilder.addType(companionSpec.build())
        typeBuilder.addFunction(RebindMethodGeneration().generate(false))
        typeBuilder.addFunctions(TypeConversionMethodsGeneration(useSuspend).generate())
        typeBuilder.addType(builderBuilder.build())

        return FileSpec.get(holder.`package`, typeBuilder.build())

    }

    private fun toMap(holder: BaseEntityHolder): List<FunSpec> {
        val nullCheck = CodeBlock.builder().beginControlFlow("if(obj == null)").addStatement("return mutableMapOf()").endControlFlow().build()
        val nullCheckList = CodeBlock.builder().beginControlFlow("if(obj == null)").addStatement("return listOf()").endControlFlow().build()

        return Arrays.asList(FunSpec.builder("toMap").addModifiers(KModifier.PUBLIC)
                .addParameter("obj", holder.entityTypeName.copy(nullable = true)).returns(TypeUtil.mutableMapStringAny())
                .addAnnotation(JvmStatic::class)
                .addCode(nullCheck).addStatement("var result = mutableMapOf<%T,%T>()", TypeUtil.string(), TypeUtil.any())
                .beginControlFlow("obj.mDoc.forEach")
                .beginControlFlow("if(it.value != null)").addStatement("result[it.key] = it.value!!").endControlFlow()
                .endControlFlow()
                .addStatement("return result").build(),

                FunSpec.builder("toMap").addModifiers(KModifier.PUBLIC)
                        .addParameter("obj", TypeUtil.list(holder.entityTypeName).copy(nullable = true)).addAnnotation(JvmStatic::class)
                        .returns(TypeUtil.listWithMutableMapStringAny()).addCode(nullCheckList)
                        .addStatement("var result = %T()", TypeUtil.arrayListWithMutableMapStringAny())
                        .addCode(CodeBlock.builder()
                                .beginControlFlow("for(entry in obj)")
                                .addStatement("var temp = mutableMapOf<%T,%T>()", TypeUtil.string(), TypeUtil.any())
                                .addStatement("temp.putAll(%N.toMap(entry)!!)", holder.entitySimpleName)
                                .addStatement("result.add(temp)", holder.entitySimpleName).endControlFlow().build())
                        .addStatement("return result").build())
    }

    private fun fromMap(holder: BaseEntityHolder): List<FunSpec> {
        val nullCheck = CodeBlock.builder().beginControlFlow("if(obj == null)").addStatement("return null").endControlFlow().build()

        return Arrays.asList(FunSpec.builder("fromMap").addModifiers(KModifier.PUBLIC)
                .addParameter("obj", TypeUtil.mutableMapStringAnyNullable().copy(nullable = true)).addAnnotation(JvmStatic::class)
                .returns(holder.entityTypeName.copy(nullable = true)).addCode(nullCheck)
                .addStatement("return %T(obj)", holder.entityTypeName).build(),

                FunSpec.builder("fromMap").addModifiers(KModifier.PUBLIC).addAnnotation(JvmStatic::class)
                        .addParameter("obj", TypeUtil.listWithMutableMapStringAnyNullable().copy(nullable = true))
                        .returns(TypeUtil.list(holder.entityTypeName).copy(nullable = true)).addCode(nullCheck)
                        .addStatement("var result = %T()", TypeUtil.arrayList(holder.entityTypeName))
                        .addCode(CodeBlock.builder().beginControlFlow("for(entry in obj)")
                                .addStatement("result.add(%N(entry))", holder.entitySimpleName)
                                .endControlFlow().build()).addStatement("return result").build()
        )
    }

    private fun constructorMap(): FunSpec {
        return FunSpec.constructorBuilder().addModifiers(KModifier.PUBLIC).addParameter("doc", TypeUtil.mutableMapStringAnyNullable()).addStatement("rebind(ensureTypes(doc))").build()
    }

    private fun constructorDefault(): FunSpec {
        // Add default constructor to allow property-based creators in Jackson
        return FunSpec.constructorBuilder().addModifiers(KModifier.PUBLIC).callThisConstructor("mutableMapOf()") .build()
    }

    private fun create(holder: WrapperEntityHolder): List<FunSpec> {

        return Arrays.asList(
                FunSpec.builder("create").addModifiers(KModifier.PUBLIC).addParameter("doc", TypeUtil.mutableMapStringAnyNullable()).addAnnotation(JvmStatic::class).addStatement("return %N(doc)",
                        holder.entitySimpleName).returns(holder.entityTypeName).build(),
                FunSpec.builder("create").addModifiers(KModifier.PUBLIC).addAnnotation(JvmStatic::class).addStatement("return %N(%T())",
                        holder.entitySimpleName, TypeUtil.hashMapStringAnyNullable()).returns(holder.entityTypeName).build()
        )
    }

}
