package com.schwarz.crystalcore.model.source

import com.schwarz.crystalapi.Field
import com.squareup.kotlinpoet.TypeName

interface ISourceField {

    val simpleName: String
    val packageName: String

    val fullQualifiedName: String

    val fieldAnnotation: Field

    val javaToKotlinType: TypeName

    val baseType: TypeName

    fun parseMetaType(list: Boolean, subEntity: String?): TypeName
}
