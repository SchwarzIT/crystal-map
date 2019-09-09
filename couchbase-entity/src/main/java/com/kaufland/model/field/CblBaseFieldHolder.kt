package com.kaufland.model.field

import com.kaufland.util.ConversionUtil
import com.kaufland.util.FieldExtractionUtil
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import kaufland.com.coachbasebinderapi.Field
import org.apache.commons.lang3.text.WordUtils
import javax.lang.model.type.TypeMirror

/**
 * Created by sbra0902 on 21.06.17.
 */

abstract class CblBaseFieldHolder(val dbField: String, private val mField: Field) {

    val typeMirror: TypeMirror? = FieldExtractionUtil.typeMirror(mField)

    open val isIterable: Boolean
        get() = mField.list

    val isDefault: Boolean
        get() = !mField.defaultValue.isEmpty() && !isConstant

    val isConstant: Boolean
        get() = mField.readonly

    val constantName: String
        get() = ConversionUtil.convertCamelToUnderscore(dbField).toUpperCase()

    val defaultValue: String
        get() = mField.defaultValue


    fun accessorSuffix(): String {
        return WordUtils.capitalize(dbField.replace("_".toRegex(), " ")).replace(" ".toRegex(), "")
    }

    abstract fun getter(dbName: String?, useMDocChanges: Boolean): FunSpec

    abstract fun setter(dbName: String?, entityTypeName: TypeName, useMDocChanges: Boolean): FunSpec?

    abstract fun createFieldConstant(): List<PropertySpec>
}
