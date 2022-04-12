package com.kaufland.model.entity

import com.kaufland.model.source.ISourceModel
import com.kaufland.util.TypeUtil
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import kaufland.com.coachbasebinderapi.Entity
import kotlin.reflect.jvm.internal.impl.load.java.sources.JavaSourceElement

class EntityHolder(val dbName: String, val modifierOpen: Boolean, val entityType: Entity.Type, sourceModel: ISourceModel) :
    BaseEntityHolder(sourceModel) {

    fun dbNameProperty(): PropertySpec {
        val builder = PropertySpec.builder("_dbName", TypeUtil.string(), KModifier.PUBLIC)
            .getter(FunSpec.getterBuilder().addStatement("return %S", dbName).build())

        return builder.build()
    }
}
