package com.schwarz.crystalprocessor.model.entity

import com.schwarz.crystalprocessor.model.source.ISourceModel
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName

class BaseModelHolder(sourceElement: ISourceModel) : BaseEntityHolder(sourceElement) {

    override val entitySimpleName: String
        get() = sourceClazzSimpleName + "BaseWrapper"

    val representTypeName: TypeName
        get() = ClassName(sourcePackage, interfaceSimpleName).nestedClass("Represent")
}
