package com.schwarz.crystalcore.generation.model

import com.schwarz.crystalcore.model.entity.BaseEntityHolder
import com.schwarz.crystalcore.util.TypeUtil
import com.squareup.kotlinpoet.PropertySpec

object CblReduceGeneration {
    fun <T> onlyIncludeProperty(holder: BaseEntityHolder<T>): PropertySpec {
        val spec =
            PropertySpec.builder(
                PROPERTY_ONLY_INCLUDES,
                TypeUtil.list(TypeUtil.string()).copy(nullable = true),
            )

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
