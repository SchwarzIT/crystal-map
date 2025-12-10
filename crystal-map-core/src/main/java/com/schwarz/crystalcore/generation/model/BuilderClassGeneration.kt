package com.schwarz.crystalcore.generation.model

import com.schwarz.crystalcore.model.entity.BaseEntityHolder
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

object BuilderClassGeneration {
    fun <T> generateBaseBuilder(holder: BaseEntityHolder<T>): TypeSpec.Builder {
        val builderBuilder =
            TypeSpec.classBuilder("Builder").primaryConstructor(
                FunSpec.constructorBuilder().addParameter("parent", holder.entityTypeName).build()
            )
        builderBuilder.addProperty(
            PropertySpec.builder("obj", holder.entityTypeName).initializer("parent").build()
        )
        builderBuilder.addFunction(
            FunSpec.builder("exit")
                .addStatement("obj.validate()")
                .addStatement("return obj")
                .returns(holder.entityTypeName).build()
        )
        return builderBuilder
    }

    fun <T> generateBuilderFun(holder: BaseEntityHolder<T>): FunSpec {
        return FunSpec.builder("builder").addStatement("return Builder(this)")
            .returns(ClassName(holder.sourcePackage, "${holder.entitySimpleName}.Builder")).build()
    }
}
