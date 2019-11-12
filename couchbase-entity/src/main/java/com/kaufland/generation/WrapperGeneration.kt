package com.kaufland.generation

import com.kaufland.model.entity.BaseEntityHolder
import com.kaufland.model.entity.WrapperEntityHolder
import com.kaufland.util.TypeUtil
import com.squareup.kotlinpoet.*
import java.util.*

class WrapperGeneration {

    fun generateModel(holder: WrapperEntityHolder): FileSpec {

        var companionSpec = TypeSpec.companionObjectBuilder()

        var builderBuilder = BuilderClassGeneration.generateBaseBuilder(holder)

        val typeBuilder = TypeSpec.classBuilder(holder.entitySimpleName).addSuperinterface(TypeUtil.mapSupport()).addModifiers(KModifier.PUBLIC)
                .addFunction(CblDefaultGeneration.addDefaults(holder))
                .addFunction(CblConstantGeneration.addConstants(holder))
                .addFunction(MapSupportGeneration.toMap(holder))
                .addProperty(PropertySpec.builder("mDoc", TypeUtil.mutableMapStringObject()).addModifiers(KModifier.PRIVATE).mutable().initializer("%T()", TypeUtil.hashMapStringObject()).build())
                .addFunction(contructor()).superclass(holder.sourceElement!!.asType().asTypeName())
                .addFunction(BuilderClassGeneration.generateBuilderFun())

        for (fieldHolder in holder.allFields) {

            companionSpec.addProperties(fieldHolder.createFieldConstant())
            typeBuilder.addProperty(fieldHolder.property(null, holder.abstractParts, false))
            fieldHolder.builderSetter(null, holder.`package`, holder.entitySimpleName, false)?.let {
                builderBuilder.addFunction(it)
            }
        }

        companionSpec.addFunctions(fromMap(holder))
        companionSpec.addFunctions(toMap(holder))
        companionSpec.addFunctions(create(holder))
        typeBuilder.addType(companionSpec.build())
        typeBuilder.addFunction(RebindMethodGeneration().generate(false))
        typeBuilder.addFunctions(TypeConversionMethodsGeneration().generate())
        typeBuilder.addType(builderBuilder.build())

        return FileSpec.get(holder.`package`, typeBuilder.build())

    }

    private fun toMap(holder: BaseEntityHolder): List<FunSpec> {
        val nullCheck = CodeBlock.builder().beginControlFlow("if(obj == null)").addStatement("return null").endControlFlow().build()

        return Arrays.asList(FunSpec.builder("toMap").addModifiers(KModifier.PUBLIC)
                .addParameter("obj", holder.entityTypeName.copy(nullable = true)).returns(TypeUtil.mutableMapStringObject().copy(nullable = true))
                .addAnnotation(JvmStatic::class)
                .addCode(nullCheck).addStatement("var result = %T()", TypeUtil.hashMapStringObject())
                .addStatement("result.putAll(obj.mDoc)").addStatement("return result").build(),

                FunSpec.builder("toMap").addModifiers(KModifier.PUBLIC)
                        .addParameter("obj", TypeUtil.list(holder.entityTypeName).copy(nullable = true)).addAnnotation(JvmStatic::class)
                        .returns(TypeUtil.listWithMutableMapStringObject().copy(nullable = true)).addCode(nullCheck)
                        .addStatement("var result = %T()", TypeUtil.arrayListWithHashMapStringObject())
                        .addCode(CodeBlock.builder()
                                .beginControlFlow("for(entry in obj)")
                                .addStatement("var temp = %T()", TypeUtil.hashMapStringObject())
                                .addStatement("temp.putAll(%N.toMap(entry)!!)", holder.entitySimpleName)
                                .addStatement("result.add(temp)", holder.entitySimpleName).endControlFlow().build())
                        .addStatement("return result").build())
    }

    private fun fromMap(holder: BaseEntityHolder): List<FunSpec> {
        val nullCheck = CodeBlock.builder().beginControlFlow("if(obj == null)").addStatement("return null").endControlFlow().build()

        return Arrays.asList(FunSpec.builder("fromMap").addModifiers(KModifier.PUBLIC)
                .addParameter("obj", TypeUtil.mutableMapStringObject().copy(nullable = true)).addAnnotation(JvmStatic::class)
                .returns(holder.entityTypeName.copy(nullable = true)).addCode(nullCheck)
                .addStatement("return %T(obj)", holder.entityTypeName).build(),

                FunSpec.builder("fromMap").addModifiers(KModifier.PUBLIC).addAnnotation(JvmStatic::class)
                        .addParameter("obj", TypeUtil.listWithMutableMapStringObject().copy(nullable = true))
                        .returns(TypeUtil.list(holder.entityTypeName).copy(nullable = true)).addCode(nullCheck)
                        .addStatement("var result = %T()", TypeUtil.arrayList(holder.entityTypeName))
                        .addCode(CodeBlock.builder().beginControlFlow("for(entry in obj)")
                                .addStatement("result.add(%N(entry))", holder.entitySimpleName)
                                .endControlFlow().build()).addStatement("return result").build()
        )
    }

    private fun contructor(): FunSpec {
        return FunSpec.constructorBuilder().addModifiers(KModifier.PUBLIC).addParameter("doc", TypeUtil.mutableMapStringObject()).addStatement("rebind(doc)").build()
    }

    private fun create(holder: WrapperEntityHolder): List<FunSpec> {

        return Arrays.asList(
                FunSpec.builder("create").addModifiers(KModifier.PUBLIC).addParameter("doc", TypeUtil.mutableMapStringObject()).addAnnotation(JvmStatic::class).addStatement("return %N(doc)",
                        holder.entitySimpleName).returns(holder.entityTypeName).build(),
                FunSpec.builder("create").addModifiers(KModifier.PUBLIC).addAnnotation(JvmStatic::class).addStatement("return %N(%T())",
                        holder.entitySimpleName, TypeUtil.hashMapStringObject()).returns(holder.entityTypeName).build()
        )
    }

}
