package com.kaufland.util

import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.TypeMirror

import kaufland.com.coachbasebinderapi.Field
import javax.management.Query

object FieldExtractionUtil {

    fun typeMirror(annotation: Field): TypeMirror? {
        try {
            annotation.type
        } catch (mte: MirroredTypeException) {
            return mte.typeMirror
        }

        return null
    }
}
