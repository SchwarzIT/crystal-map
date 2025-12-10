package com.schwarz.crystalcore.model.entity

import com.schwarz.crystalcore.model.source.ISourceModel
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName

class BaseModelHolder<T>(
    sourceElement: ISourceModel<T>,
) : BaseEntityHolder<T>(sourceElement) {
    override val entitySimpleName: String
        get() = sourceClazzSimpleName + "BaseWrapper"

    val representTypeName: TypeName
        get() = ClassName(sourcePackage, interfaceSimpleName).nestedClass("Represent")
}
