package com.kaufland.model.field

import com.kaufland.util.ConversionUtil
import com.kaufland.util.FieldExtractionUtil
import com.kaufland.util.TypeUtil
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

    val typeMirror: TypeMirror = FieldExtractionUtil.typeMirror(mField)

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

    val comment: Array<String>
        get() = mField.comment


    fun accessorSuffix(): String {
        return WordUtils.uncapitalize(WordUtils.capitalize(dbField.replace("_".toRegex(), " ")).replace(" ".toRegex(), ""))
    }

    abstract fun property(dbName: String?, possibleOverrides: Set<String>, useMDocChanges: Boolean): PropertySpec

    abstract fun builderSetter(dbName: String?, packageName: String, entitySimpleName: String, useMDocChanges: Boolean): FunSpec?

    abstract fun createFieldConstant(): List<PropertySpec>
}
