package com.kaufland.validation.mapper

import com.kaufland.Logger
import com.kaufland.model.entity.BaseEntityHolder
import com.kaufland.model.entity.BaseModelHolder
import com.kaufland.model.entity.EntityHolder
import com.kaufland.model.entity.WrapperEntityHolder
import com.kaufland.model.mapper.MapperHolder
import kaufland.com.coachbasebinderapi.deprecated.DeprecatedField

class MapperValidation(val logger: Logger, val mappers: MutableMap<String, MapperHolder>) {

//    private fun validateQuery(baseEntityHolder: BaseEntityHolder) {
//
//        for (query in baseEntityHolder.queries) {
//            for (field in query.fields) {
//                if (!baseEntityHolder.fields.containsKey(field) && !baseEntityHolder.fieldConstants.containsKey(field)) {
//                    logger.error("query param [$field] is not a part of this entity", baseEntityHolder.sourceElement)
//                }
//            }
//        }
//    }
//
//    private fun validateDeprecated(baseEntityHolder: BaseEntityHolder) {
//
//        baseEntityHolder.deprecated?.let { deprecated ->
//            deprecated.replacedByTypeMirror?.toString()?.apply {
//                if (this != Void::class.java.canonicalName && !wrapperModels.containsKey(this) && !entityModels.containsKey(this)) {
//                    logger.error("replacement [$this] is not an entity/wrapper", baseEntityHolder.sourceElement)
//                }
//            }
//
//            validateDeprecatedFields(deprecated.deprecatedFields, deprecated.replacedByTypeMirror?.toString(), baseEntityHolder)
//        }
//    }
//
//    private fun validateDeprecatedFields(deprecatedFields: Map<String, DeprecatedField>, replacingModel: String?, model: BaseEntityHolder) {
//        val replacingModel: BaseEntityHolder? = replacingModel?.let {
//            wrapperModels[it] ?: entityModels[it]
//        }
//
//        for (field in deprecatedFields) {
//            if (!model.fields.containsKey(field.key) && !model.fieldConstants.containsKey(field.key)) {
//                logger.error("replacement field [${field.key}] does not exists", model.sourceElement)
//            }
//
//            field.value.replacedBy?.let { replacement ->
//                val replacingIncludedInModel = model.fields.containsKey(replacement) || model.fieldConstants.containsKey(replacement)
//                val replacementIncludedReplacingModel = replacingModel?.let { it.fields.containsKey(replacement) || it.fieldConstants?.containsKey(replacement) }
//                        ?: false
//
//                if (!replacingIncludedInModel && !replacementIncludedReplacingModel) {
//                    logger.error("replacement [${replacement}] for field [${field.key}] does not exists", model.sourceElement)
//                }
//            }
//        }
//
//
//    }
//
//    private fun validateDocId(baseEntityHolder: BaseEntityHolder) {
//        baseEntityHolder.docId?.let {
//
//            //we always need our variables between %
//            if (it.pattern.count { it == '%' } % 2 != 0) {
//                logger.error("all variables in a DocId should be wrapped in % e.G. %variable%", baseEntityHolder.sourceElement)
//            }
//
//            for (segment in it.segments) {
//                for (field in segment.fields) {
//                    if (!baseEntityHolder.fieldConstants.containsKey(field) && !baseEntityHolder.fields.containsKey(field)) {
//                        logger.error("field [${field}] for DocId generation does not exists", baseEntityHolder.sourceElement)
//                    }
//                }
//                segment.customSegment?.apply {
//                    if (!it.customSegments.containsKey(name)) {
//                        logger.error("DocIdSegment annotated [${name}] not found in DocId", baseEntityHolder.sourceElement)
//                    }
//                }
//
//                if (segment.customSegment == null && (segment.segment.contains('(') || segment.segment.contains(')'))) {
//                    logger.error("It looks like you try to use a DocIdSegment which not exists", baseEntityHolder.sourceElement)
//                }
//
//
//            }
//        }
//    }

    fun postValidate(): Boolean {

//        for (wrapper in wrapperModels) {
//            validateQuery(wrapper.value)
//            validateDeprecated(wrapper.value)
//            validateDocId(wrapper.value)
//        }
//
//
//        for (entity in entityModels) {
//            validateQuery(entity.value)
//            validateDeprecated(entity.value)
//            validateDocId(entity.value)
//        }

        return logger.hasErrors().not()
    }
}
