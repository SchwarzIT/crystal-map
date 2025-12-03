package com.schwarz.crystalcore.model.mapper

import com.schwarz.crystalapi.mapify.Mapifyable
import com.schwarz.crystalapi.mapify.Mapper
import com.schwarz.crystalcore.model.mapper.type.MapifyElementType
import com.schwarz.crystalcore.model.source.ISourceDeclaringName
import java.io.Serializable

class MapifyHolder<T>(val mapifyElement: MapifyElementType<T>) :
    MapifyElementType<T> by mapifyElement {
    val typeHandleMode: TypeHandleMode =
        when {
            declaringName.isPlainType() -> TypeHandleMode.PLAIN
            declaringName.isAnnotationPresent(Mapper::class.java) -> TypeHandleMode.MAPPER
            declaringName.isTypeVar() -> TypeHandleMode.TYPEVAR
            isMapifyable(declaringName) -> TypeHandleMode.MAPIFYABLE
            else -> TypeHandleMode.UNKNOWN
        }

    private fun isMapifyable(declaringName: ISourceDeclaringName): Boolean {
        return (
            declaringName.isProcessingType() ||
                declaringName.isPlainType() ||
                declaringName.isAssignable(List::class) ||
                declaringName.isAssignable(Map::class) ||
                declaringName.isAssignable(Serializable::class) ||
                declaringName.isAnnotationPresent(Mapifyable::class.java) ||
                declaringName.isAnnotationPresent(Mapper::class.java) || declaringName.isTypeVar()
            ) && declaringName.typeParams.all { isMapifyable(it) }
    }

    enum class TypeHandleMode {
        PLAIN,
        MAPPER,
        MAPIFYABLE,
        TYPEVAR,
        UNKNOWN
    }
}
