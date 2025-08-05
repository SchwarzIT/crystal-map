package com.schwarz.crystalcore.model.mapper

import com.schwarz.crystalcore.model.source.ISourceDeclaringName
import com.schwarz.crystalcore.model.source.ISourceMapper
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName

class MapperHolder<T>(val sourceElement: ISourceMapper<T>) {

    val fields: MutableMap<String, MapifyHolder<T>> = mutableMapOf()

    val sourceClazzSimpleName: String
        get() = sourceElement.sourceClazzSimpleName

    val typeParams: List<ISourceDeclaringName> = sourceElement.typeParams

    val sourceClazzTypeName: TypeName = sourceElement.sourceClazzTypeName

    val declaringName = sourceElement.declaringName

    val targetMapperSimpleName: String
        get() = sourceClazzSimpleName + "Mapper"

    val `package`: String
        get() = sourceElement.sourcePackage

    val targetMapperTypeName: TypeName
        get() = ClassName(`package`, targetMapperSimpleName)
}
