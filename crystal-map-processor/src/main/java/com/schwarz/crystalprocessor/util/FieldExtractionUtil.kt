package com.schwarz.crystalprocessor.util

import com.schwarz.crystalapi.BasedOn
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.TypeMirror

import com.schwarz.crystalapi.Field
import com.schwarz.crystalapi.TypeConverterImporter
import com.schwarz.crystalapi.mapify.Mapifyable
import com.schwarz.crystalapi.deprecated.Deprecated
import javax.lang.model.type.MirroredTypesException

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
            } else {
                null
            }
        } catch (mte: MirroredTypeException) {
            mte.typeMirror
        }
    }

    fun typeMirror(annotation: Mapifyable): TypeMirror? {
        return try {
            annotation.value
            throw Exception("Expected to get a MirroredTypeException")
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

    fun typeMirror(annotation: TypeConverterImporter): List<TypeMirror> {
        val result = mutableListOf<TypeMirror>()

        try {
            if (annotation.typeConverterExporter != null) {
                throw Exception("Expected to get a MirroredTypesException")
            }
        } catch (mte: MirroredTypesException) {
            result.addAll(mte.typeMirrors)
        }

        return result
    }
}
