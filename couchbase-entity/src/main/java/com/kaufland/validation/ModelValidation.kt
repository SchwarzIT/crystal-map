package com.kaufland.validation

import com.kaufland.Logger
import com.kaufland.model.entity.BaseEntityHolder
import com.kaufland.model.entity.BaseModelHolder
import com.kaufland.model.entity.EntityHolder
import com.kaufland.model.entity.WrapperEntityHolder
import kaufland.com.coachbasebinderapi.deprecated.DeprecatedField

class ModelValidation(val logger: Logger, val baseModels: MutableMap<String, BaseModelHolder>, val wrapperModels: MutableMap<String, WrapperEntityHolder>, val entityModels: MutableMap<String, EntityHolder>) {

    private fun validateQuery(baseEntityHolder: BaseEntityHolder) {

        for (query in baseEntityHolder.queries) {
            for (field in query.fields) {
                if (!baseEntityHolder.fields.containsKey(field) && !baseEntityHolder.fieldConstants.containsKey(field)) {
                    logger.error("query param [$field] is not a part of this entity", baseEntityHolder.sourceElement)
                }
            }
        }
    }

    private fun validateDeprecated(baseEntityHolder: BaseEntityHolder) {

        baseEntityHolder.deprecated?.let { deprecated ->
            deprecated.replacedByTypeMirror?.toString()?.apply {
                if (this != Void::class.java.canonicalName && !wrapperModels.containsKey(this) && !entityModels.containsKey(this)) {
                    logger.error("replacement [$this] is not an entity/wrapper", baseEntityHolder.sourceElement)
                }
            }

            validateDeprecatedFields(deprecated.deprecatedFields, deprecated.replacedByTypeMirror?.toString(), baseEntityHolder)
        }
    }

    private fun validateDeprecatedFields(deprecatedFields: Map<String, DeprecatedField>, replacingModel: String?, model: BaseEntityHolder) {
        val replacingModel: BaseEntityHolder? = replacingModel?.let {
            wrapperModels[it] ?: entityModels[it]
        }

        for (field in deprecatedFields) {
            if (!model.fields.containsKey(field.key) && !model.fieldConstants.containsKey(field.key)){
                logger.error("replacement field [${field.key}] does not exists", model.sourceElement)
            }

            field.value.replacedBy?.let { replacement ->
                val replacingIncludedInModel = model.fields.containsKey(replacement) || model.fieldConstants.containsKey(replacement)
                val replacementIncludedReplacingModel = replacingModel?.let { it.fields.containsKey(replacement) ||  it.fieldConstants?.containsKey(replacement)} ?: false

                if(!replacingIncludedInModel  && !replacementIncludedReplacingModel){
                    logger.error("replacement [${replacement}] for field [${field.key}] does not exists", model.sourceElement)
                }
            }
        }


    }

    private fun validateDocId(baseEntityHolder: BaseEntityHolder){
        baseEntityHolder.docId?.let {

            //we always need our variables between %
            if(it.pattern.count { it == '%' } % 2 != 0){
                logger.error("all variables in a DocId should be wrapped in % e.G. %variable%", baseEntityHolder.sourceElement)
            }

            for (concatedField in it.concatedFields) {
                if(!baseEntityHolder.fieldConstants.containsKey(concatedField) && !baseEntityHolder.fields.containsKey(concatedField)){
                    logger.error("field [${concatedField}] for DocId generation does not exists", baseEntityHolder.sourceElement)
                }
            }
        }
    }

    fun postValidate(): Boolean {

        for (wrapper in wrapperModels) {
            validateQuery(wrapper.value)
            validateDeprecated(wrapper.value)
            validateDocId(wrapper.value)
        }


        for (entity in entityModels) {
            validateQuery(entity.value)
            validateDeprecated(entity.value)
            validateDocId(entity.value)
        }

        return logger.hasErrors().not()
    }
}
