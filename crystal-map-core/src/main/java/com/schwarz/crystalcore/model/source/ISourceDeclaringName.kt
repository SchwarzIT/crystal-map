package com.schwarz.crystalcore.model.source

import com.schwarz.crystalapi.mapify.Mapifyable
import com.squareup.kotlinpoet.TypeName

interface ISourceDeclaringName {

    val name: String

    val typeParams: List<ISourceDeclaringName>

    fun asTypeName(): TypeName?
    fun asFullTypeName(): TypeName?

    fun hasEmptyConstructor(): Boolean

    fun isPlainType(): Boolean

    fun isTypeVar(): Boolean

    fun isNullable(): Boolean

    fun isProcessingType(): Boolean

    fun isAssignable(clazz: Class<*>): Boolean

    fun <A : Annotation?> getAnnotation(annotationType: Class<A>?): A?

    fun typeAsDeclaringName(mapifyable: Mapifyable): ISourceDeclaringName?
}
