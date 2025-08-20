package com.schwarz.crystalcore.model.source

import com.squareup.kotlinpoet.TypeName

interface IClassModel<T> {

    val sourceClazzSimpleName: String
    val sourceClazzTypeName: TypeName
    val sourcePackage: String
    val typeName: TypeName

    val source: T

    val accessible: Boolean

    fun asDeclaringName(optinalIndexes: Array<Int>): ISourceDeclaringName
}
