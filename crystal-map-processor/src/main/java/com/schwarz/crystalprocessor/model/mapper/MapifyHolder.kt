package com.schwarz.crystalprocessor.model.mapper

import com.schwarz.crystalprocessor.ProcessingContext
import com.schwarz.crystalprocessor.ProcessingContext.isAssignable
import com.schwarz.crystalprocessor.model.mapper.type.MapifyElementType
import com.schwarz.crystalapi.mapify.Mapifyable
import com.schwarz.crystalapi.mapify.Mapper
import java.io.Serializable

class MapifyHolder(val mapifyElement: MapifyElementType) :
    MapifyElementType by mapifyElement {

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
