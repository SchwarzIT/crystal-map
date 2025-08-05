package com.schwarz.crystalcore.model.mapper

import com.schwarz.crystalapi.mapify.Mapifyable
import com.schwarz.crystalapi.mapify.Mapper
import com.schwarz.crystalcore.model.mapper.type.MapifyElementType
import com.schwarz.crystalcore.model.source.ISourceDeclaringName
import java.io.Serializable

class MapifyHolder<T>(val mapifyElement: MapifyElementType<T>) :
    MapifyElementType<T> by mapifyElement {

    val typeHandleMode: TypeHandleMode = when {
        declaringName.isPlainType() -> TypeHandleMode.PLAIN
        declaringName.getAnnotation(Mapper::class.java) != null -> TypeHandleMode.MAPPER
        declaringName.isTypeVar() -> TypeHandleMode.TYPEVAR
        isMapifyable(declaringName) -> TypeHandleMode.MAPIFYABLE
        else -> TypeHandleMode.UNKNOWN
    }

    private fun isMapifyable(declaringName: ISourceDeclaringName): Boolean {
        return (declaringName.isProcessingType() || declaringName.isAssignable(List::class.java) || declaringName.isAssignable(Map::class.java) || declaringName.isAssignable(Serializable::class.java) || declaringName.getAnnotation(Mapifyable::class.java) != null) && declaringName.typeParams.all { isMapifyable(it) }
    }

    enum class TypeHandleMode {
        PLAIN,
        MAPPER,
        MAPIFYABLE,
        TYPEVAR,
        UNKNOWN
    }
}
