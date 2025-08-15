package com.schwarz.crystalprocessor.model.source

import com.schwarz.crystalapi.Field
import com.schwarz.crystalcore.model.source.ISourceField
import com.schwarz.crystalcore.javaToKotlinType
import com.schwarz.crystalprocessor.util.FieldExtractionUtil
import com.schwarz.crystalcore.util.TypeUtil.list
import com.schwarz.crystalcore.util.TypeUtil.map
import com.schwarz.crystalcore.util.TypeUtil.mapStringAnyNullable
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import javax.lang.model.type.TypeMirror

class SourceField(val fieldAnnotation: Field) : ISourceField {

    private val typeMirror: TypeMirror = FieldExtractionUtil.typeMirror(fieldAnnotation)

    private fun getSimpleName(type: TypeMirror): String {
        val parts = type.toString().split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return if (parts.size > 1) parts[parts.size - 1] else parts[0]
    }

    private fun getPackage(type: TypeMirror): String {
        val lastIndexOf = type.toString().lastIndexOf(".")
        return if (lastIndexOf >= 0) type.toString().substring(0, lastIndexOf) else type.toString()
    }

    override val simpleName: String
        get() = getSimpleName(typeMirror)
    override val packageName: String
        get() = getPackage(typeMirror)
    override val readonly: Boolean = fieldAnnotation.readonly
    override val name: String = fieldAnnotation.name
    override val list: Boolean = fieldAnnotation.list
    override val defaultValue: String = fieldAnnotation.defaultValue
    override val mandatory: Boolean = fieldAnnotation.mandatory
    override val comment: Array<String> = fieldAnnotation.comment

    override val fullQualifiedName: String
        get() = typeMirror.toString()

    override val javaToKotlinType = typeMirror.asTypeName().javaToKotlinType()
    override val baseType: TypeName = typeMirror.asTypeName()

    override fun parseMetaType(list: Boolean, subEntity: String?): TypeName {
        return parseMetaType(list, true, subEntity)
    }

    private fun parseMetaType(list: Boolean, convertMap: Boolean, subEntity: String?): TypeName {
        val simpleName = if (subEntity != null && subEntity.contains(simpleName)) subEntity else simpleName

        var baseType: TypeName?

        if (typeMirror.toString().split(".").size == 1) {
            baseType = typeMirror.asTypeName()
        } else {
            try {
                baseType = ClassName(packageName, simpleName)
            } catch (e: IllegalArgumentException) {
                baseType = typeMirror.asTypeName()
            }
        }

        if (convertMap && baseType!!.javaToKotlinType() == map()) {
            baseType = mapStringAnyNullable()
        }

        return if (list) {
            list(baseType!!.javaToKotlinType())
        } else {
            baseType!!.javaToKotlinType()
        }
    }
}
