package com.kaufland.model.mapper

import com.kaufland.ProcessingContext
import com.kaufland.ProcessingContext.isAssignable
import com.kaufland.model.mapper.type.MapifyElementType
import kaufland.com.coachbasebinderapi.mapify.Mapifyable
import kaufland.com.coachbasebinderapi.mapify.Mapper
import java.io.Serializable
import java.util.*
import javax.annotation.processing.ProcessingEnvironment

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
