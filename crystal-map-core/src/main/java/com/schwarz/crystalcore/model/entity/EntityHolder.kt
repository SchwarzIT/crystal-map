package com.schwarz.crystalcore.model.entity

import com.schwarz.crystalapi.Entity
import com.schwarz.crystalcore.model.source.ISourceModel
import com.schwarz.crystalcore.util.TypeUtil
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec

class EntityHolder<T>(val dbName: String, val modifierOpen: Boolean, val entityType: Entity.Type, sourceModel: ISourceModel<T>) :
    BaseEntityHolder<T>(sourceModel) {
    fun dbNameProperty(): PropertySpec {
        val builder =
            PropertySpec.builder("_dbName", TypeUtil.string(), KModifier.PUBLIC)
                .getter(FunSpec.getterBuilder().addStatement("return %S", dbName).build())

        return builder.build()
    }
}
