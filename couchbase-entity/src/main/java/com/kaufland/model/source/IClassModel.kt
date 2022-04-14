package com.kaufland.model.source

import com.squareup.kotlinpoet.TypeName

interface IClassModel {

    val sourceClazzSimpleName: String
    val sourceClazzTypeName: TypeName
    val sourcePackage: String
}
