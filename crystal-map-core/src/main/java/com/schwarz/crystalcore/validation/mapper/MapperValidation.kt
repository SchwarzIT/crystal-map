package com.schwarz.crystalcore.validation.mapper

import com.schwarz.crystalapi.mapify.Mapifyable
import com.schwarz.crystalcore.ILogger
import com.schwarz.crystalcore.model.source.IClassModel
import com.schwarz.crystalcore.model.mapper.MapifyHolder
import com.schwarz.crystalcore.model.mapper.MapperHolder
import com.schwarz.crystalcore.model.mapper.type.MapifyElementTypeField
import com.schwarz.crystalcore.model.mapper.type.MapifyElementTypeGetterSetter

class MapperValidation<T>(val logger: ILogger<T>, val mappers: MutableMap<String, MapperHolder<T>>) {

    fun postValidate(): Boolean {
        for (mapper in mappers) {
            for (field in mapper.value.fields) {
                val element: IClassModel<T> = (field.value.mapifyElement as? MapifyElementTypeField)?.let { it.element } ?: (field.value.mapifyElement as? MapifyElementTypeGetterSetter)?.let { it.getterSetter.getterElement } ?: throw Exception("unknown kind")

                if (field.value.typeHandleMode == MapifyHolder.TypeHandleMode.UNKNOWN) {
                    logger.error("the field ${field.key} is not ${Mapifyable::class.java.simpleName} or plain or any other parseable type. ", element.source)
                }
            }
        }

        return logger.hasErrors().not()
    }
}
