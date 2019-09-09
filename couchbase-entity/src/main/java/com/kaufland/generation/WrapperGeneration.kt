package com.kaufland.generation

import com.kaufland.model.entity.BaseEntityHolder
import com.kaufland.model.entity.WrapperEntityHolder
import com.kaufland.util.TypeUtil
import com.squareup.kotlinpoet.*
import java.util.*

class WrapperGeneration {

    fun generateModel(holder: WrapperEntityHolder): FileSpec {

        val typeBuilder = TypeSpec.classBuilder(holder.entitySimpleName).addSuperinterface(TypeUtil.mapSupport()).addModifiers(KModifier.PUBLIC).addFunction(CblDefaultGeneration.addDefaults(holder)).addFunction(CblConstantGeneration.addConstants(holder)).addFunction(MapSupportGeneration.toMap(holder)).addFunctions(create(holder)).addProperty("mDoc", TypeUtil.mapStringObject(), KModifier.PRIVATE).addFunction(contructor()).superclass(holder.sourceElement!!.asType().asTypeName())

        for (fieldHolder in holder.allFields) {

            typeBuilder.addProperties(fieldHolder.createFieldConstant())
            typeBuilder.addFunction(fieldHolder.getter(null, false))

            val setter = fieldHolder.setter(null, holder.entityTypeName, false)
            if (setter != null) {
                typeBuilder.addFunction(setter)
            }
        }

        typeBuilder.addFunction(RebindMethodGeneration().generate(false))
        typeBuilder.addFunctions(fromMap(holder))
        typeBuilder.addFunctions(toMap(holder))
        typeBuilder.addFunctions(TypeConversionMethodsGeneration().generate())

        return FileSpec.get(holder.`package`, typeBuilder.build())

    }

    private fun toMap(holder: BaseEntityHolder): List<FunSpec> {
        val nullCheck = CodeBlock.builder().beginControlFlow("if(obj == null)").addStatement("return null").endControlFlow().build()

        return Arrays.asList(FunSpec.builder("toMap").addModifiers(KModifier.PUBLIC).addParameter( "obj", holder.entityTypeName).returns(TypeUtil.mapStringObject()).addCode(nullCheck).addStatement("\$T result = new \$T()", TypeUtil.hashMapStringObject(), TypeUtil.hashMapStringObject())
                .addStatement("result.putAll(obj.mDoc)").addStatement("return result").build(),

                FunSpec.builder("toMap").addModifiers(KModifier.PUBLIC).addParameter("obj", TypeUtil.list(holder.entityTypeName)).returns(TypeUtil.listWithMapStringObject()).addCode(nullCheck).addStatement("\$T result = new \$T()", TypeUtil.listWithMapStringObject(), TypeUtil.arrayListWithMapStringObject()).addCode(CodeBlock.builder().beginControlFlow("for(\$N entry : obj)", holder.entitySimpleName).addStatement("\$T temp = new \$T()", TypeUtil.hashMapStringObject(), TypeUtil.hashMapStringObject()).addStatement("temp.putAll(((\$N)entry).toMap(entry))", holder.entitySimpleName).addStatement("result.add(temp)", holder.entitySimpleName).endControlFlow().build()).addStatement("return result").build())
    }

    private fun fromMap(holder: BaseEntityHolder): List<FunSpec> {
        val nullCheck = CodeBlock.builder().beginControlFlow("if(obj == null)").addStatement("return null").endControlFlow().build()

        return Arrays.asList(FunSpec.builder("fromMap").addModifiers(KModifier.PUBLIC).addParameter( "obj", TypeUtil.mapStringObject()).returns(holder.entityTypeName).addCode(nullCheck).addStatement("return new \$T(obj)", holder.entityTypeName).build(),

                FunSpec.builder("fromMap").addModifiers(KModifier.PUBLIC).addParameter("obj", TypeUtil.listWithMapStringObject()).returns(TypeUtil.list(holder.entityTypeName)).addCode(nullCheck).addStatement("\$T result = new \$T()", TypeUtil.list(holder.entityTypeName),TypeUtil.arrayList(holder.entityTypeName)).addCode(CodeBlock.builder().beginControlFlow("for(\$T entry : obj)", TypeUtil.mapStringObject()).addStatement("result.add(new \$N(entry))", holder.entitySimpleName).endControlFlow().build()).addStatement("return result").build()
        )
    }

    private fun contructor(): FunSpec {
        return FunSpec.constructorBuilder().addModifiers(KModifier.PUBLIC).addParameter( "doc", TypeUtil.mapStringObject()).addStatement("rebind(doc)").build()
    }

    private fun create(holder: WrapperEntityHolder): List<FunSpec> {

        return Arrays.asList(
                FunSpec.builder("create").addModifiers(KModifier.PUBLIC).addParameter( "doc", TypeUtil.mapStringObject()).addStatement("return new \$N (doc)",
                        holder.entitySimpleName).returns(holder.entityTypeName).build(),
                FunSpec.builder("create").addModifiers(KModifier.PUBLIC).addStatement("return new \$N (new \$T())",
                        holder.entitySimpleName, TypeUtil.hashMapStringObject()).returns(holder.entityTypeName).build()
        )
    }

}
