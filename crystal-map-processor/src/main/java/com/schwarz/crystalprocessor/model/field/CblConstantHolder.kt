package com.schwarz.crystalprocessor.model.field

import com.schwarz.crystalprocessor.generation.model.KDocGeneration
import com.schwarz.crystalprocessor.generation.model.TypeConversionMethodsGeneration
import com.schwarz.crystalprocessor.javaToKotlinType
import com.schwarz.crystalprocessor.model.deprecated.DeprecatedModel
import com.schwarz.crystalprocessor.util.ConversionUtil
import com.schwarz.crystalprocessor.util.TypeUtil
import com.squareup.kotlinpoet.*

import java.util.Arrays

import com.schwarz.crystalapi.Field

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
            .getter(FunSpec.getterBuilder().addStatement("return " + TypeConversionMethodsGeneration.READ_METHOD_NAME + "(mDoc.get(%N),%N, %T::class)!!", constantName, constantName, fieldType).build())

        deprecated?.addDeprecated(dbField, builder)
        if (comment.isNotEmpty()) {
            builder.addKdoc(KDocGeneration.generate(comment))
        }

        return builder.build()
    }

    override fun createFieldConstant(): List<PropertySpec> {

        val fieldAccessorConstant = PropertySpec.builder(constantName, String::class, KModifier.FINAL, KModifier.PUBLIC).initializer("%S", dbField).addAnnotation(JvmField::class).build()

        return Arrays.asList(
            fieldAccessorConstant,
            PropertySpec.builder(constantValueAccessorName, typeMirror.asTypeName().javaToKotlinType(), KModifier.FINAL, KModifier.PUBLIC).initializer(
                ConversionUtil.convertStringToDesiredFormat(typeMirror, constantValue)
            ).addAnnotation(JvmField::class).build()
        )
    }

    override fun builderSetter(dbName: String?, packageName: String, entitySimpleName: String, useMDocChanges: Boolean, deprecated: DeprecatedModel?): FunSpec? {
        return null
    }
}
