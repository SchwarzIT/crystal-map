package com.schwarz.crystalprocessor.model.field

import com.schwarz.crystalprocessor.generation.model.KDocGeneration
import com.schwarz.crystalprocessor.javaToKotlinType
import com.schwarz.crystalprocessor.model.deprecated.DeprecatedModel
import com.schwarz.crystalprocessor.util.ConversionUtil
import com.schwarz.crystalprocessor.util.TypeUtil
import com.squareup.kotlinpoet.*

import java.util.Arrays

import com.schwarz.crystalapi.Field
import com.schwarz.crystalapi.util.CrystalWrap
import com.schwarz.crystalprocessor.model.typeconverter.TypeConverterHolderForEntityGeneration
import com.schwarz.crystalprocessor.model.typeconverter.TypeConverterProcessingException

/**
 * Created by sbra0902 on 21.06.17.
 */

class CblConstantHolder(field: Field) : CblBaseFieldHolder(field.name, field) {

    val constantValue: String = field.defaultValue

    val constantValueAccessorName = "DOC_$constantName"

    override val fieldType: TypeName = TypeUtil.parseMetaType(typeMirror, isIterable, null)

    override fun interfaceProperty(
        isOverride: Boolean,
        deprecated: DeprecatedModel?
    ): PropertySpec {
        val modifiers = listOfNotNull(KModifier.PUBLIC, KModifier.OVERRIDE.takeIf { isOverride })
        val builder = PropertySpec.builder(accessorSuffix(), fieldType, modifiers)
        deprecated?.addDeprecated(dbField, builder)
        return builder.build()
    }

    override fun property(
        dbName: String?,
        possibleOverrides: Set<String>,
        useMDocChanges: Boolean,
        deprecated: DeprecatedModel?,
        typeConvertersByConvertedClass: Map<TypeName, TypeConverterHolderForEntityGeneration>
    ): PropertySpec {
        val mDocPhrase = if (useMDocChanges) "mDocChanges, mDoc" else "mDoc, mutableMapOf()"
        val builder =
            PropertySpec.builder(accessorSuffix(), fieldType, KModifier.PUBLIC, KModifier.OVERRIDE)

        if (isNonConvertibleClass) {
            builder.getter(
                FunSpec.getterBuilder().addStatement(
                    "return %T.get<%T>($mDocPhrase, %N)!!",
                    CrystalWrap::class,
                    fieldType,
                    constantName
                ).build()
            )
        } else {
            val typeConverterHolder = typeConvertersByConvertedClass[fieldType]
                ?: throw TypeConverterProcessingException("Missing type conversion for $fieldType")

            builder.getter(
                FunSpec.getterBuilder().addStatement(
                    "return %T.get($mDocPhrase, %N, %T)!!",
                    CrystalWrap::class,
                    constantName,
                    typeConverterHolder.instanceClassTypeName
                ).build()
            )
        }

        deprecated?.addDeprecated(dbField, builder)
        if (comment.isNotEmpty()) {
            builder.addKdoc(KDocGeneration.generate(comment))
        }

        return builder.build()
    }

    override fun createFieldConstant(): List<PropertySpec> {
        val fieldAccessorConstant =
            PropertySpec.builder(constantName, String::class, KModifier.FINAL, KModifier.PUBLIC)
                .initializer("%S", dbField).addAnnotation(JvmField::class).build()

        return Arrays.asList(
            fieldAccessorConstant,
            PropertySpec.builder(
                constantValueAccessorName,
                typeMirror.asTypeName().javaToKotlinType(),
                KModifier.FINAL,
                KModifier.PUBLIC
            ).initializer(
                ConversionUtil.convertStringToDesiredFormat(typeMirror, constantValue)
            ).addAnnotation(JvmField::class).build()
        )
    }

    override fun builderSetter(
        dbName: String?,
        packageName: String,
        entitySimpleName: String,
        useMDocChanges: Boolean,
        deprecated: DeprecatedModel?
    ): FunSpec? {
        return null
    }
}
