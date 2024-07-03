package com.schwarz.crystalprocessor.model.entity

import com.schwarz.crystalprocessor.model.source.ISourceModel
import com.schwarz.crystalprocessor.util.TypeUtil
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.schwarz.crystalapi.Entity

class EntityHolder(
    val dbName: String,
    val collection: String,
    val modifierOpen: Boolean,
    val entityType: Entity.Type,
    sourceModel: ISourceModel
) :
    BaseEntityHolder(sourceModel) {

    fun dbNameProperty(): PropertySpec {
        val builder = PropertySpec.builder("_dbName", TypeUtil.string(), KModifier.PUBLIC)
            .getter(FunSpec.getterBuilder().addStatement("return %S", dbName).build())

        return builder.build()
    }

    fun collectionProperty(): PropertySpec {
        val builder = PropertySpec.builder("_collection", TypeUtil.string(), KModifier.PUBLIC)
            .getter(FunSpec.getterBuilder().addStatement("return %S", collection).build())

        return builder.build()
    }
}
