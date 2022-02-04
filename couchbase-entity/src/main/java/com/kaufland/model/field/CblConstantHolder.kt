package com.kaufland.model.field

import com.kaufland.generation.model.KDocGeneration
import com.kaufland.generation.model.TypeConversionMethodsGeneration
import com.kaufland.javaToKotlinType
import com.kaufland.model.deprecated.DeprecatedModel
import com.kaufland.util.ConversionUtil
import com.kaufland.util.TypeUtil
import com.squareup.kotlinpoet.*

import java.util.Arrays

import kaufland.com.coachbasebinderapi.Field

/**
 * Created by sbra0902 on 21.06.17.
 */

class CblConstantHolder(field: Field) : CblBaseFieldHolder(field.name, field) {

    val constantValue: String = field.defaultValue

    val constantValueAccessorName = "DOC_$constantName"

    override val fieldType: TypeName = TypeUtil.parseMetaType(typeMirror, isIterable, null)

    override fun interfaceProperty(isOverride: Boolean): PropertySpec {
        val modifiers = listOfNotNull(KModifier.PUBLIC, KModifier.OVERRIDE.takeIf { isOverride })
        return PropertySpec.builder(accessorSuffix(), fieldType, modifiers).build()
    }

    override fun property(dbName: String?, possibleOverrides: Set<String>, useMDocChanges: Boolean, deprecated: DeprecatedModel?): PropertySpec {

        val builder = PropertySpec.builder(accessorSuffix(), fieldType, KModifier.PUBLIC, KModifier.OVERRIDE)
            .getter(FunSpec.getterBuilder().addStatement("return " + TypeConversionMethodsGeneration.READ_METHOD_NAME + "(mDoc.get(%N), %T::class)!!", constantName, fieldType).build())

        if (comment.isNotEmpty()) {
            builder.addKdoc(KDocGeneration.generate(comment))
        }

        return builder.build()
    }

    override fun createFieldConstant(): List<PropertySpec> {

        val fieldAccessorConstant = PropertySpec.builder(constantName, String::class, KModifier.FINAL, KModifier.PUBLIC).initializer("%S", dbField).addAnnotation(JvmField::class).build()

        return Arrays.asList(
            fieldAccessorConstant,
            PropertySpec.builder(constantValueAccessorName, typeMirror.asTypeName().javaToKotlinType(), KModifier.FINAL, KModifier.PUBLIC).initializer(ConversionUtil.convertStringToDesiredFormat(typeMirror, constantValue)).addAnnotation(JvmField::class).build()
        )
    }

    override fun builderSetter(dbName: String?, packageName: String, entitySimpleName: String, useMDocChanges: Boolean, deprecated: DeprecatedModel?): FunSpec? {
        return null
    }
}
