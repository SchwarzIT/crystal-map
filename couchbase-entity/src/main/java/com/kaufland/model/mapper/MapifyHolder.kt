package com.kaufland.model.mapper

import com.kaufland.ProcessingContext
import com.kaufland.ProcessingContext.asDeclaringName
import com.kaufland.ProcessingContext.asTypeElement
import com.kaufland.ProcessingContext.isAssignable
import com.kaufland.javaToKotlinType
import com.kaufland.model.mapper.type.MapifyElementType
import com.kaufland.util.FieldExtractionUtil
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import kaufland.com.coachbasebinderapi.mapify.Mapify
import kaufland.com.coachbasebinderapi.mapify.Mapifyable
import kaufland.com.coachbasebinderapi.mapify.Mapper
import java.io.Serializable
import java.math.BigDecimal
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier


class MapifyHolder(val mapifyElement : MapifyElementType, env: ProcessingEnvironment)
    : MapifyElementType by mapifyElement{

    val typeHandleMode: TypeHandleMode = when {
        declaringName.isPlainType() -> TypeHandleMode.PLAIN
        declaringName.asTypeElement()?.getAnnotation(Mapper::class.java) != null -> TypeHandleMode.MAPPER
        declaringName.isTypeVar() -> TypeHandleMode.TYPEVAR
        isMapifyable(declaringName) -> TypeHandleMode.MAPIFYABLE
        else -> TypeHandleMode.UNKNOWN
    }

    private fun isMapifyable(declaringName: ProcessingContext.DeclaringName): Boolean {
        return (declaringName.isProcessingType() || declaringName.asTypeElement()?.let { it.isAssignable(List::class.java) || it.isAssignable(Map::class.java) || it.isAssignable(Serializable::class.java) || it.getAnnotation(Mapifyable::class.java) != null } ?: false) && declaringName.typeParams.all { isMapifyable(it) }
    }

    enum class TypeHandleMode {
        PLAIN,
        MAPPER,
        MAPIFYABLE,
        TYPEVAR,
        UNKNOWN
    }
}
