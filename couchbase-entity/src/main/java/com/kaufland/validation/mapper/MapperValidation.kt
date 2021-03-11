package com.kaufland.validation.mapper

import com.kaufland.Logger
import com.kaufland.model.entity.BaseEntityHolder
import com.kaufland.model.entity.BaseModelHolder
import com.kaufland.model.entity.EntityHolder
import com.kaufland.model.entity.WrapperEntityHolder
import com.kaufland.model.mapper.MapifyHolder
import com.kaufland.model.mapper.MapperHolder
import kaufland.com.coachbasebinderapi.deprecated.DeprecatedField

class MapperValidation(val logger: Logger, val mappers: MutableMap<String, MapperHolder>) {

    fun postValidate(): Boolean {

        for (mapper in mappers) {
            for (field in mapper.value.fields) {
                if(field.value.typeHandleMode == MapifyHolder.TypeHandleMode.UNKNOWN){
                    logger.error("the field is not mapifyable or plain or any other parseable type. ", field.value.element)
                }
            }
        }

        return logger.hasErrors().not()
    }
}
