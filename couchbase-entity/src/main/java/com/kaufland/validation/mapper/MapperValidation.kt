package com.kaufland.validation.mapper

import com.kaufland.Logger
import com.kaufland.model.mapper.MapifyHolder
import com.kaufland.model.mapper.MapperHolder
import com.kaufland.model.mapper.type.MapifyElementTypeField
import com.kaufland.model.mapper.type.MapifyElementTypeGetterSetter
import kaufland.com.coachbasebinderapi.mapify.Mapifyable
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
