package com.schwarz.crystalcore.model.source

import com.squareup.kotlinpoet.TypeName
import kotlin.reflect.KClass

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

    fun isAssignable(clazz: KClass<*>): Boolean

    fun <A : Annotation?, B> getAnnotationRepresent(annotationType: Class<A>?): B?

    fun <A : Annotation?> isAnnotationPresent(annotationType: Class<A>): Boolean
}
