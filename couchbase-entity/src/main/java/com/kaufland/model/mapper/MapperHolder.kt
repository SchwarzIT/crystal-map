package com.kaufland.model.mapper

import com.kaufland.model.accessor.CblGenerateAccessorHolder
import com.kaufland.model.deprecated.DeprecatedModel
import com.kaufland.model.field.CblBaseFieldHolder
import com.kaufland.model.field.CblConstantHolder
import com.kaufland.model.field.CblFieldHolder
import com.kaufland.model.id.DocIdHolder
import com.kaufland.model.query.CblQueryHolder
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import com.sun.tools.javac.code.Symbol
import kaufland.com.coachbasebinderapi.query.Query

import java.util.ArrayList

import javax.lang.model.element.Element

class MapperHolder(val sourceElement: Element) {

    val fields: MutableMap<String, MapifyHolder> = mutableMapOf()

    val sourceClazzSimpleName: String
        get() = (sourceElement as Symbol.ClassSymbol).simpleName.toString()

    val sourceClazzTypeName: TypeName = ClassName(`package`, sourceClazzSimpleName)

    val targetMapperSimpleName: String
        get() = sourceClazzSimpleName + "Mapper"

    val `package`: String
        get() = (sourceElement as Symbol.ClassSymbol).packge().toString()

    val targetMapperTypeName: TypeName
        get() = ClassName(`package`, targetMapperSimpleName)

}
