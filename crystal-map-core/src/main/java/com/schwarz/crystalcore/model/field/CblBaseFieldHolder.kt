package com.schwarz.crystalcore.model.field

import com.schwarz.crystalcore.model.deprecated.DeprecatedModel
import com.schwarz.crystalcore.model.source.ISourceField
import com.schwarz.crystalcore.model.typeconverter.TypeConverterHolderForEntityGeneration
import com.schwarz.crystalcore.model.typeconverter.nonConvertibleClassesTypeNames
import com.schwarz.crystalcore.util.ConversionUtil
import com.schwarz.crystalcore.util.TypeUtil
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import org.apache.commons.lang3.text.WordUtils

/**
 * Created by sbra0902 on 21.06.17.
 */

abstract class CblBaseFieldHolder(val dbField: String, val mField: ISourceField) {

    open val isIterable: Boolean
        get() = mField.list

    val isDefault: Boolean
        get() = mField.defaultValue.isNotEmpty() && !isConstant

    val isConstant: Boolean
        get() = mField.readonly

    val constantName: String
        get() = ConversionUtil.convertCamelToUnderscore(dbField).uppercase()

    val defaultValue: String
        get() = mField.defaultValue

    val mandatory: Boolean
        get() = mField.mandatory

    val comment: Array<String>
        get() = mField.comment

    val simpleName: String = mField.simpleName

    fun ensureTypeEscape(value: String): String {
        return if (mField.javaToKotlinType == TypeUtil.string()) {
            "\"" + value + "\""
        } else {
            value
        }
    }

    abstract val fieldType: TypeName

    val isNonConvertibleClass: Boolean
        get() {
            val rawFieldType = (fieldType as? ParameterizedTypeName)?.rawType ?: fieldType
            return nonConvertibleClassesTypeNames.contains(rawFieldType)
        }

    fun accessorSuffix(): String {
        return WordUtils.uncapitalize(
            WordUtils.capitalize(dbField.replace("_".toRegex(), " ")).replace(" ".toRegex(), "")
        )
    }

    abstract fun interfaceProperty(isOverride: Boolean = false, deprecated: DeprecatedModel?): PropertySpec

    abstract fun property(
        dbName: String?,
        possibleOverrides: Set<String>,
        useMDocChanges: Boolean,
        deprecated: DeprecatedModel?,
        typeConvertersByConvertedClass: Map<TypeName, TypeConverterHolderForEntityGeneration>
    ): PropertySpec

    abstract fun builderSetter(
        dbName: String?,
        packageName: String,
        entitySimpleName: String,
        useMDocChanges: Boolean,
        deprecated: DeprecatedModel?
    ): FunSpec?

    abstract fun createFieldConstant(): List<PropertySpec>
}
