package com.kaufland.generation.model

import com.kaufland.model.entity.BaseEntityHolder
import com.kaufland.util.TypeUtil
import com.squareup.kotlinpoet.PropertySpec

object CblReduceGeneration {

    fun onlyIncludeProperty(holder: BaseEntityHolder): PropertySpec {

        val spec = PropertySpec.builder(PROPERTY_ONLY_INCLUDES, TypeUtil.list(TypeUtil.string()).copy(nullable = true))

        if (holder.isReduced) {
            val allDBFields = holder.allFields.map { it.dbField }
            val placeHolders = allDBFields.joinToString { "%S" }
            spec.initializer("listOf($placeHolders)", *allDBFields.toTypedArray())
        } else {
            spec.initializer("null")
        }

        return spec.build()
    }

    const val PROPERTY_ONLY_INCLUDES = "INCLUDED_FILTER"
}
