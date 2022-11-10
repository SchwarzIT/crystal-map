package com.schwarz.crystalprocessor.generation

import com.schwarz.crystalprocessor.model.entity.BaseEntityHolder
import com.schwarz.crystalprocessor.util.TypeUtil
import com.squareup.kotlinpoet.*
import com.schwarz.crystalapi.mapify.Mapifyable

object MapifyableImplGeneration {

    data class Config(val clazzName: String, val typeParam: TypeName, val fromMap: (FunSpec.Builder) -> FunSpec, val toMap: (FunSpec.Builder) -> FunSpec, val modifiers: Array<KModifier> = emptyArray())

    fun typeSpec(config: Config) =
        TypeSpec.classBuilder(config.clazzName)
            .addModifiers(*config.modifiers)
            .addSuperinterface(TypeUtil.iMapifyable(config.typeParam))
            .addFunction(
                config.fromMap(
                    FunSpec.builder("fromMap")
                        .addModifiers(KModifier.OVERRIDE)
                        .addParameter("map", TypeUtil.mapStringAny())
                        .returns(config.typeParam)
                )
            )
            .addFunction(
                config.toMap(
                    FunSpec.builder("toMap")
                        .addModifiers(KModifier.OVERRIDE)
                        .addParameter("obj", config.typeParam)
                        .returns(TypeUtil.mapStringAny())
                )
            )
            .build()

    fun typeSpec(holder: BaseEntityHolder): TypeSpec {
        val config = Config(
            clazzName = "Mapper",
            typeParam = holder.entityTypeName,
            fromMap = { it.addStatement("return %T.create(map.toMutableMap())", holder.entityTypeName).build() },
            toMap = { it.addStatement("return obj.toMap()", holder.entityTypeName).build() }
        )

        return typeSpec(config)
    }

    fun impl(holder: BaseEntityHolder): AnnotationSpec {
        return AnnotationSpec.builder(Mapifyable::class).addMember("value = %T::class", ClassName(holder.sourcePackage, "${holder.entitySimpleName}.Mapper"))
            .build()
    }
}
