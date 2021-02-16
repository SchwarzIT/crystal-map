package com.kaufland.util

import kaufland.com.coachbasebinderapi.BaseModel
import kaufland.com.coachbasebinderapi.BasedOn
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.TypeMirror

import kaufland.com.coachbasebinderapi.Field
import kaufland.com.coachbasebinderapi.deprecated.Deprecated
import javax.lang.model.type.MirroredTypesException
import javax.management.Query

object FieldExtractionUtil {

    fun typeMirror(annotation: Field): TypeMirror {
        try {
            annotation.type
            throw Exception("Expected to get a MirroredTypeException")
        } catch (mte: MirroredTypeException) {
            return mte.typeMirror
        }
    }

    fun typeMirror(annotation: Deprecated): TypeMirror? {
        return try {
            annotation.replacedBy

            if (annotation.replacedBy != null) {
                throw Exception("Expected to get a MirroredTypeException")
            } else null
        } catch (mte: MirroredTypeException) {
            mte.typeMirror
        }
    }

    fun typeMirror(annotation: BasedOn): List<TypeMirror> {

        val result = mutableListOf<TypeMirror>()

        try {
            if (annotation.value.isNotEmpty()) {
                throw Exception("Expected to get a MirroredTypesException")
            }
        } catch (mte: MirroredTypesException) {
            result.addAll(mte.typeMirrors)
        }

        return result
    }
}
