package com.schwarz.crystalcore.model.source

import com.squareup.kotlinpoet.TypeName

interface ISourceField {

    val simpleName: String
    val packageName: String

    val readonly: Boolean

    val name: String

    val list: Boolean

    val defaultValue: String

    val mandatory: Boolean

    val comment: Array<String>

    val fullQualifiedName: String

    val javaToKotlinType: TypeName

    val baseType: TypeName

    fun parseMetaType(list: Boolean, subEntity: String?): TypeName
}
