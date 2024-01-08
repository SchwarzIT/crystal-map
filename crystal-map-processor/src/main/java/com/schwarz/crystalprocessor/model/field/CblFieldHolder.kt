package com.schwarz.crystalprocessor.model.field

import com.schwarz.crystalprocessor.generation.model.KDocGeneration
import com.schwarz.crystalprocessor.model.deprecated.DeprecatedModel
import com.schwarz.crystalprocessor.util.TypeUtil
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.schwarz.crystalapi.Field
import com.schwarz.crystalapi.util.CrystalWrap
import org.apache.commons.lang3.StringUtils

class CblFieldHolder(field: Field, allWrappers: List<String>) :
    CblBaseFieldHolder(field.name, field) {

    private var subEntityPackage: String? = null

    var subEntitySimpleName: String? = null
        private set

    var isSubEntityIsTypeParam: Boolean = false
        private set

    override var isIterable: Boolean = false

    private val subEntityTypeName: TypeName
        get() = ClassName(subEntityPackage!!, subEntitySimpleName!!)

    private val isTypeOfSubEntity: Boolean
        get() = !StringUtils.isBlank(subEntitySimpleName)

    override val fieldType: TypeName =
        TypeUtil.parseMetaType(typeMirror, isIterable, subEntitySimpleName)

    init {
        if (allWrappers.contains(typeMirror.toString())) {

            subEntitySimpleName = TypeUtil.getSimpleName(typeMirror) + "Wrapper"
            subEntityPackage = TypeUtil.getPackage(typeMirror)
            isSubEntityIsTypeParam = field.list
        }
        if (field.list) {
            isIterable = true
        }
    }

    override fun interfaceProperty(
        isOverride: Boolean,
        deprecated: DeprecatedModel?
    ): PropertySpec {
        var returnType = TypeUtil.parseMetaType(typeMirror, isIterable, subEntitySimpleName)

        if (mandatory.not()) {
            returnType = returnType.copy(nullable = true)
        }

        val modifiers = listOfNotNull(KModifier.PUBLIC, KModifier.OVERRIDE.takeIf { isOverride })
        val propertyBuilder =
            PropertySpec.builder(accessorSuffix(), returnType, modifiers)
                .mutable(true)
        deprecated?.addDeprecated(dbField, propertyBuilder)
        return propertyBuilder.build()
    }

    override fun property(
        dbName: String?,
        possibleOverrides: Set<String>,
        useMDocChanges: Boolean,
        deprecated: DeprecatedModel?
    ): PropertySpec {
        var returnType = TypeUtil.parseMetaType(typeMirror, isIterable, subEntitySimpleName)

        if (mandatory.not()) {
            returnType = returnType.copy(nullable = true)
        }

        val propertyBuilder = PropertySpec.builder(
            accessorSuffix(),
            returnType,
            KModifier.PUBLIC,
            KModifier.OVERRIDE
        ).mutable(true)

        val getter = FunSpec.getterBuilder()
        val setter = FunSpec.setterBuilder().addParameter("value", String::class)

        deprecated?.addDeprecated(dbField, propertyBuilder)

        val mDocPhrase = if (useMDocChanges) "mDocChanges, mDoc" else "mDoc, mutableMapOf()"

        if (isTypeOfSubEntity) {
            if (isIterable) {
                getter.addStatement(
                    "return %T.getList<%T>($mDocPhrase, %N, %T::class, {%T.fromMap(it) ?: emptyList()})".forceCastIfMandatory(mandatory),
                    CrystalWrap::class,
                    subEntityTypeName,
                    constantName,
                    subEntityTypeName,
                    subEntityTypeName
                )
                setter.addStatement(
                    "%T.setList(%N, %N, value, %T::class, {%T.toMap(it)})",
                    CrystalWrap::class,
                    if (useMDocChanges) "mDocChanges" else "mDoc",
                    constantName,
                    subEntityTypeName,
                    subEntityTypeName
                )
            } else {
                getter.addStatement(
                    "return %T.get<%T>($mDocPhrase, %N, %T::class, {%T.fromMap(it)})".forceCastIfMandatory(mandatory),
                    CrystalWrap::class,
                    subEntityTypeName,
                    constantName,
                    subEntityTypeName,
                    subEntityTypeName
                )
                setter.addStatement(
                    "%T.set(%N, %N, value, %T::class, {%T.toMap(it)})",
                    CrystalWrap::class,
                    if (useMDocChanges) "mDocChanges" else "mDoc",
                    constantName,
                    subEntityTypeName,
                    subEntityTypeName
                )
            }
        } else {
            val forTypeConversion = evaluateClazzForTypeConversion()
            if (isIterable) {
                getter.addStatement(
                    "return %T.getList<%T>($mDocPhrase, %N, %T::class)".forceCastIfMandatory(mandatory),
                    CrystalWrap::class,
                    fieldType,
                    constantName,
                    forTypeConversion
                )
            } else {
                getter.addStatement(
                    "return %T.get<%T>($mDocPhrase, %N, %T::class)".forceCastIfMandatory(mandatory),
                    CrystalWrap::class,
                    fieldType,
                    constantName,
                    forTypeConversion
                )
            }

            setter.addStatement(
                "%T.set(%N, %N, value, %T::class)",
                CrystalWrap::class,
                if (useMDocChanges) "mDocChanges" else "mDoc",
                constantName,
                forTypeConversion
            )
        }

        if (comment.isNotEmpty()) {
            propertyBuilder.addKdoc(KDocGeneration.generate(comment))
        }

        return propertyBuilder.setter(setter.build()).getter(getter.build()).build()
    }

    override fun builderSetter(
        dbName: String?,
        packageName: String,
        entitySimpleName: String,
        useMDocChanges: Boolean,
        deprecated: DeprecatedModel?
    ): FunSpec {
        val fieldType = TypeUtil.parseMetaType(typeMirror, isIterable, subEntitySimpleName)
        val builder =
            FunSpec.builder("set" + accessorSuffix().capitalize()).addModifiers(KModifier.PUBLIC)
                .addParameter("value", fieldType)
                .returns(ClassName(packageName, "$entitySimpleName.Builder"))

        if (this.comment.isNotEmpty()) {
            builder.addKdoc(KDocGeneration.generate(comment))
        }

        if (deprecated?.addDeprecatedBuilderSetter(
                dbField,
                builder
            ) == true
        ) {
            builder.addStatement("throw %T()", UnsupportedOperationException::class)
        } else {
            builder.addStatement("obj.${accessorSuffix()} = value")
            builder.addStatement("return this")
        }

        return builder.build()
    }

    override fun createFieldConstant(): List<PropertySpec> {
        val fieldAccessorConstant =
            PropertySpec.builder(constantName, String::class, KModifier.FINAL, KModifier.PUBLIC)
                .initializer("%S", dbField).addAnnotation(JvmField::class).build()
        return listOf(fieldAccessorConstant)
    }

    fun evaluateClazzForTypeConversion(): TypeName {
        return if (isIterable) {
            if (TypeUtil.isMap(fieldType)) {
                TypeUtil.string()
            } else {
                fieldType
            }
        } else {
            TypeUtil.parseMetaType(typeMirror, isIterable, false, subEntitySimpleName)
        }
    }

    private fun String.forceCastIfMandatory(mandatory: Boolean): String {
        if (mandatory) {
            return "$this!!"
        }
        return this
    }
}
