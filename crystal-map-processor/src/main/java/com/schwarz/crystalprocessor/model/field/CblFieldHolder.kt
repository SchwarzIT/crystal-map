package com.schwarz.crystalprocessor.model.field

import com.schwarz.crystalprocessor.generation.model.KDocGeneration
import com.schwarz.crystalprocessor.generation.model.TypeConversionMethodsGeneration
import com.schwarz.crystalprocessor.model.deprecated.DeprecatedModel
import com.schwarz.crystalprocessor.util.TypeUtil
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.schwarz.crystalapi.Field
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

    override fun interfaceProperty(isOverride: Boolean): PropertySpec {
        val returnType = TypeUtil.parseMetaType(typeMirror, isIterable, subEntitySimpleName)
            .copy(nullable = true)
        val modifiers = listOfNotNull(KModifier.PUBLIC, KModifier.OVERRIDE.takeIf { isOverride })
        return PropertySpec.builder(accessorSuffix(), returnType.copy(true), modifiers)
            .mutable(true).build()
    }

    override fun property(
        dbName: String?,
        possibleOverrides: Set<String>,
        useMDocChanges: Boolean,
        deprecated: DeprecatedModel?
    ): PropertySpec {
        val returnType = TypeUtil.parseMetaType(typeMirror, isIterable, subEntitySimpleName)
            .copy(nullable = true)

        val propertyBuilder = PropertySpec.builder(
            accessorSuffix(), returnType.copy(true), KModifier.PUBLIC, KModifier.OVERRIDE
        ).mutable(true)

        val getter = FunSpec.getterBuilder()
        val setter = FunSpec.setterBuilder().addParameter("value", String::class)

        deprecated?.addDeprecated(dbField, propertyBuilder)

        val docName = if (useMDocChanges) "mDocChanges" else "mDoc"

        if (isTypeOfSubEntity) {
            val castType =
                if (isSubEntityIsTypeParam) TypeUtil.listWithMutableMapStringAnyNullable() else TypeUtil.mutableMapStringAnyNullable()

            if (useMDocChanges) {
                getter.addCode(
                    CodeBlock.builder()
                        .beginControlFlow("if(mDocChanges.containsKey(%N))", constantName)
                        .addStatement(
                            "return·%T.fromMap(mDocChanges.get(%N) as? %T)",
                            subEntityTypeName,
                            constantName,
                            castType
                        ).endControlFlow().build()
                )
            }
            getter.addCode(
                CodeBlock.builder().beginControlFlow("if(mDoc.containsKey(%N))", constantName)
                    /** In case the key for the subentity is set but the value is null it would result in a classcastexception since null can't be cast to any type*/
                    .addStatement(
                        "return·%T.fromMap(mDoc.get(%N) as? %T)",
                        subEntityTypeName,
                        constantName,
                        castType
                    ).endControlFlow().build()
            )

            getter.addStatement("return null")

            setter.addStatement(
                "%N.put(%N, %T.toMap(value))", docName, constantName, subEntityTypeName
            )
        } else {

            val forTypeConversion = evaluateClazzForTypeConversion()
            if (useMDocChanges) {
                getter.addCode(
                    CodeBlock.builder()
                        .beginControlFlow("if(mDocChanges.containsKey(%N))", constantName)
                        .addStatement(
                            "return " + TypeConversionMethodsGeneration.READ_METHOD_NAME + "(mDocChanges.get(%N), %N, %T::class)",
                            constantName, constantName,
                            forTypeConversion
                        ).endControlFlow().build()
                )
            }

            getter.addCode(
                CodeBlock.builder().beginControlFlow("if(mDoc.containsKey(%N))", constantName)
                    .addStatement(
                        "return " + TypeConversionMethodsGeneration.READ_METHOD_NAME + "(mDoc.get(%N), %N, %T::class)",
                        constantName, constantName,
                        forTypeConversion
                    ).endControlFlow().build()
            )

            getter.addStatement("return null")

            setter.addStatement(
                "%N.put(%N, " + TypeConversionMethodsGeneration.WRITE_METHOD_NAME + "(value, %N, %T::class))",
                docName,
                constantName, constantName,
                forTypeConversion
            )
        }

        if (comment.isNotEmpty()) {
            propertyBuilder.addKdoc(KDocGeneration.generate(comment))
        }

        return propertyBuilder.setter(setter.build()).getter(getter.build()).build()
    }

    fun ensureType(resultType: TypeName, format: String, vararg args: Any?): CodeBlock {
        val forTypeConversion = evaluateClazzForTypeConversion()
        return CodeBlock.of(
            "${TypeConversionMethodsGeneration.WRITE_METHOD_NAME}<%T>($format, %T::class)",
            resultType,
            *args,
            forTypeConversion
        )
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
                dbField, builder
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
        } else TypeUtil.parseMetaType(typeMirror, isIterable, false, subEntitySimpleName)
    }
}
