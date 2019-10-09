package com.kaufland.generation

import com.kaufland.model.entity.BaseEntityHolder
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

object BuilderClassGeneration {

    fun generateBaseBuilder(holder: BaseEntityHolder): TypeSpec.Builder {
        val builderBuilder = TypeSpec.classBuilder("Builder").primaryConstructor(FunSpec.constructorBuilder().addParameter("parent", holder.entityTypeName).build())
        builderBuilder.addProperty(PropertySpec.builder("obj", holder.entityTypeName).initializer("parent").build())
        builderBuilder.addFunction(FunSpec.builder("exit").addStatement("return obj").build())
        return builderBuilder
    }

    fun generateBuilderFun(): FunSpec {
       return FunSpec.builder("builder").addStatement("return Builder(this)").build()
    }

}