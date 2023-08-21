package com.schwarz.crystalprocessor.generation.model

import com.schwarz.crystalprocessor.model.entity.BaseEntityHolder
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

object BuilderClassGeneration {

    fun generateBaseBuilder(holder: BaseEntityHolder): TypeSpec.Builder {
        val builderBuilder = TypeSpec.classBuilder("Builder").primaryConstructor(FunSpec.constructorBuilder().addParameter("parent", holder.entityTypeName).build())
        builderBuilder.addProperty(PropertySpec.builder("obj", holder.entityTypeName).initializer("parent").build())
        builderBuilder.addFunction(FunSpec.builder("exit").addStatement("return obj").returns(holder.entityTypeName).build())
        return builderBuilder
    }

    fun generateBuilderFun(holder: BaseEntityHolder): FunSpec {
        return FunSpec.builder("builder").addStatement("return Builder(this)").returns(ClassName(holder.sourcePackage, "${holder.entitySimpleName}.Builder")).build()
    }
}
