package com.schwarz.crystalprocessor.validation.mapper

import com.schwarz.crystalprocessor.Logger
import com.schwarz.crystalprocessor.model.mapper.MapifyHolder
import com.schwarz.crystalprocessor.model.mapper.MapperHolder
import com.schwarz.crystalprocessor.model.mapper.type.MapifyElementTypeField
import com.schwarz.crystalprocessor.model.mapper.type.MapifyElementTypeGetterSetter
import com.schwarz.crystalapi.mapify.Mapifyable
import java.lang.Exception
import javax.lang.model.element.Element

class MapperValidation(val logger: Logger, val mappers: MutableMap<String, MapperHolder>) {

    fun postValidate(): Boolean {

        for (mapper in mappers) {
            for (field in mapper.value.fields) {

                val element: Element = (field.value.mapifyElement as? MapifyElementTypeField)?.let { it.element } ?: (field.value.mapifyElement as? MapifyElementTypeGetterSetter)?.let { it.getterSetter.getterElement } ?: throw Exception("unknown kind")

                if (field.value.typeHandleMode == MapifyHolder.TypeHandleMode.UNKNOWN) {
                    logger.error("the field is not ${Mapifyable::class.java.simpleName} or plain or any other parseable type. ", element)
                }
            }
        }

        return logger.hasErrors().not()
    }
}
