package com.schwarz.crystalcore.model

import com.schwarz.crystalcore.model.mapper.MapifyHolder
import com.schwarz.crystalcore.model.mapper.MapperHolder
import com.schwarz.crystalcore.model.mapper.type.MapifyElementTypeField
import com.schwarz.crystalcore.model.mapper.type.MapifyElementTypeGetterSetter
import com.schwarz.crystalcore.model.source.ISourceMapperModel

object MapperFactory {
    fun <T> create(mapperElement: ISourceMapperModel<T>): MapperHolder<T> {
        val result = MapperHolder(mapperElement)

        result.fields.putAll(
            mapperElement.fields.map {
                it.key to
                    MapifyHolder(
                        MapifyElementTypeField(it.value.field, it.value.mapify)
                    )
            }
        )

        mapperElement.getterSetters.filter { it.value.getterElement != null && it.value.setterElement != null }?.forEach {
            result.fields[it.key] = MapifyHolder(MapifyElementTypeGetterSetter(it.value, fieldName = it.key))
        }

        return result
    }
}
