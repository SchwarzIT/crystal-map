package com.schwarz.crystalcore.model.field

import com.schwarz.crystalapi.util.CrystalWrap
import com.schwarz.crystalcore.generation.model.KDocGeneration
import com.schwarz.crystalcore.model.deprecated.DeprecatedModel
import com.schwarz.crystalcore.model.source.ISourceField
import com.schwarz.crystalcore.model.typeconverter.TypeConverterHolderForEntityGeneration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import org.apache.commons.lang3.StringUtils

class CblFieldHolder(
    private val field: ISourceField,
    classPaths: List<String>,
    subEntityNameSuffix: String,
    private val getterCache: Boolean = false,
) : CblBaseFieldHolder(field.name, field) {
    private var subEntityPackage: String? = null

    var subEntitySimpleName: String? = null
        private set

    var isSubEntityIsTypeParam: Boolean = false
        private set

    override var isIterable: Boolean = false

    val subEntityTypeName: TypeName
        get() = ClassName(subEntityPackage!!, subEntitySimpleName!!)

    val isTypeOfSubEntity: Boolean
        get() = !StringUtils.isBlank(subEntitySimpleName)

    val isCacheable: Boolean
        get() = getterCache && !(isNonConvertibleClass && !isIterable)

    override val fieldType: TypeName = field.parseMetaType(isIterable, subEntitySimpleName)

    init {
        if (classPaths.contains(field.fullQualifiedName)) {

            subEntitySimpleName = field.simpleName + subEntityNameSuffix
            subEntityPackage = field.packageName
            isSubEntityIsTypeParam = field.list
        }
        if (field.list) {
            isIterable = true
        }
    }

    override fun interfaceProperty(
        isOverride: Boolean,
        deprecated: DeprecatedModel?,
    ): PropertySpec {
        var returnType = field.parseMetaType(isIterable, subEntitySimpleName)

        if (mandatory.not()) {
            returnType = returnType.copy(nullable = true)
        }

        val modifiers = listOfNotNull(KModifier.PUBLIC, KModifier.OVERRIDE.takeIf { isOverride })
        val propertyBuilder =
            PropertySpec
                .builder(accessorSuffix(), returnType, modifiers)
                .mutable(true)
        deprecated?.addDeprecated(dbField, propertyBuilder)
        return propertyBuilder.build()
    }

    override fun property(
        dbName: String?,
        possibleOverrides: Set<String>,
        useMDocChanges: Boolean,
        deprecated: DeprecatedModel?,
        typeConvertersByConvertedClass: Map<TypeName, TypeConverterHolderForEntityGeneration>,
    ): PropertySpec {
        var returnType = field.parseMetaType(isIterable, subEntitySimpleName)

        if (mandatory.not()) {
            returnType = returnType.copy(nullable = true)
        }

        val propertyBuilder =
            PropertySpec
                .builder(
                    accessorSuffix(),
                    returnType,
                    KModifier.PUBLIC,
                    KModifier.OVERRIDE,
                ).mutable(true)

        val getter = FunSpec.getterBuilder()
        val setter = FunSpec.setterBuilder().addParameter("value", String::class)

        deprecated?.addDeprecated(dbField, propertyBuilder)

        val mDocPhrase = if (useMDocChanges) "mDocChanges, mDoc" else "mDoc, mutableMapOf()"
        val cachedFieldName = "_cached_${accessorSuffix()}"
        val cacheGenFieldName = "_cacheGen_${accessorSuffix()}"

        if (isCacheable) {
            getter.beginControlFlow("if ($cacheGenFieldName != _cacheGen)")
            crystalWrapGetStatement(getter, mDocPhrase, typeConvertersByConvertedClass, cachedFieldName)
            getter.addStatement("$cacheGenFieldName = _cacheGen")
            getter.endControlFlow()
            if (mandatory) {
                getter.addStatement("return $cachedFieldName!!")
            } else {
                getter.addStatement("return $cachedFieldName")
            }
        } else {
            crystalWrapGetStatement(getter, mDocPhrase, typeConvertersByConvertedClass)
        }

        crystalWrapSetStatement(
            setter,
            if (useMDocChanges) "mDocChanges" else "mDoc",
            typeConvertersByConvertedClass,
            "value",
        )
        if (isCacheable) {
            setter.addStatement("_cacheGen++")
        }

        if (comment.isNotEmpty()) {
            propertyBuilder.addKdoc(KDocGeneration.generate(comment))
        }

        return propertyBuilder.setter(setter.build()).getter(getter.build()).build()
    }

    fun crystalWrapGetStatement(
        getter: FunSpec.Builder,
        mDocPhrase: String,
        typeConvertersByConvertedClass: Map<TypeName, TypeConverterHolderForEntityGeneration>,
        assignTo: String? = null,
    ) {
        val prefix = if (assignTo != null) "$assignTo = " else "return "
        val forceCast = assignTo == null && mandatory
        if (isNonConvertibleClass) {
            if (isIterable) {
                getter.addStatement(
                    "$prefix%T.getList<%T>($mDocPhrase, %N)".forceCastIfMandatory(
                        forceCast,
                    ),
                    CrystalWrap::class,
                    fieldType,
                    constantName,
                )
            } else {
                getter.addStatement(
                    "$prefix%T.get<%T>($mDocPhrase, %N)".forceCastIfMandatory(
                        forceCast,
                    ),
                    CrystalWrap::class,
                    fieldType,
                    constantName,
                )
            }
        } else if (isTypeOfSubEntity) {
            if (isIterable) {
                getter.addStatement(
                    "$prefix%T.getList<%T>($mDocPhrase, %N, {%T.fromMap(it)})".forceCastIfMandatory(
                        forceCast,
                    ),
                    CrystalWrap::class,
                    subEntityTypeName,
                    constantName,
                    subEntityTypeName,
                )
            } else {
                getter.addStatement(
                    "$prefix%T.get<%T>($mDocPhrase, %N, {%T.fromMap(it)})".forceCastIfMandatory(
                        forceCast,
                    ),
                    CrystalWrap::class,
                    subEntityTypeName,
                    constantName,
                    subEntityTypeName,
                )
            }
        } else {
            val typeConverterHolder =
                typeConvertersByConvertedClass.get(fieldType)!!
            if (isIterable) {
                getter.addStatement(
                    "$prefix%T.getList($mDocPhrase, %N, %T)".forceCastIfMandatory(
                        forceCast,
                    ),
                    CrystalWrap::class,
                    constantName,
                    typeConverterHolder.instanceClassTypeName,
                )
            } else {
                getter.addStatement(
                    "$prefix%T.get($mDocPhrase, %N, %T)".forceCastIfMandatory(
                        forceCast,
                    ),
                    CrystalWrap::class,
                    constantName,
                    typeConverterHolder.instanceClassTypeName,
                )
            }
        }
    }

    fun crystalWrapSetStatement(
        setter: FunSpec.Builder,
        mDocPhrase: String,
        typeConvertersByConvertedClass: Map<TypeName, TypeConverterHolderForEntityGeneration>,
        valueName: String,
    ) {
        if (isNonConvertibleClass) {
            if (isIterable) {
                setter.addStatement(
                    "%T.setList(%N, %N, %L)",
                    CrystalWrap::class,
                    mDocPhrase,
                    constantName,
                    valueName,
                )
            } else {
                setter.addStatement(
                    "%T.set(%N, %N, %L)",
                    CrystalWrap::class,
                    mDocPhrase,
                    constantName,
                    valueName,
                )
            }
        } else if (isTypeOfSubEntity) {
            if (isIterable) {
                setter.addStatement(
                    "%T.setList(%N, %N, %L, {%T.toMap(it)})",
                    CrystalWrap::class,
                    mDocPhrase,
                    constantName,
                    valueName,
                    subEntityTypeName,
                )
            } else {
                setter.addStatement(
                    "%T.set(%N, %N, %L, {%T.toMap(it)})",
                    CrystalWrap::class,
                    mDocPhrase,
                    constantName,
                    valueName,
                    subEntityTypeName,
                )
            }
        } else {
            val typeConverterHolder =
                typeConvertersByConvertedClass.get(fieldType)!!
            if (isIterable) {
                setter.addStatement(
                    "%T.setList(%N, %N, %L, %T)",
                    CrystalWrap::class,
                    mDocPhrase,
                    constantName,
                    valueName,
                    typeConverterHolder.instanceClassTypeName,
                )
            } else {
                setter.addStatement(
                    "%T.set(%N, %N, %L, %T)",
                    CrystalWrap::class,
                    mDocPhrase,
                    constantName,
                    valueName,
                    typeConverterHolder.instanceClassTypeName,
                )
            }
        }
    }

    override fun builderSetter(
        dbName: String?,
        packageName: String,
        entitySimpleName: String,
        useMDocChanges: Boolean,
        deprecated: DeprecatedModel?,
    ): FunSpec {
        val fieldType = field.parseMetaType(isIterable, subEntitySimpleName)
        val builder =
            FunSpec
                .builder("set" + accessorSuffix().capitalize())
                .addModifiers(KModifier.PUBLIC)
                .addParameter("value", fieldType)
                .returns(ClassName(packageName, "$entitySimpleName.Builder"))

        if (this.comment.isNotEmpty()) {
            builder.addKdoc(KDocGeneration.generate(comment))
        }

        if (deprecated?.addDeprecatedBuilderSetter(
                dbField,
                builder,
            ) == true
        ) {
            builder.addStatement("throw %T()", UnsupportedOperationException::class)
        } else {
            builder.addStatement("obj.${accessorSuffix()} = value")
            builder.addStatement("return this")
        }

        return builder.build()
    }

    fun cacheProperties(): List<PropertySpec> {
        if (!isCacheable) return emptyList()
        var cacheType = field.parseMetaType(isIterable, subEntitySimpleName).copy(nullable = true)
        return listOf(
            PropertySpec
                .builder("_cached_${accessorSuffix()}", cacheType, KModifier.PRIVATE)
                .mutable()
                .initializer("null")
                .build(),
            PropertySpec
                .builder("_cacheGen_${accessorSuffix()}", Long::class, KModifier.PRIVATE)
                .mutable()
                .initializer("-1L")
                .build(),
        )
    }

    override fun createFieldConstant(): List<PropertySpec> {
        val fieldAccessorConstant =
            PropertySpec
                .builder(constantName, String::class, KModifier.FINAL, KModifier.PUBLIC)
                .initializer("%S", dbField)
                .addAnnotation(JvmField::class)
                .build()
        return listOf(fieldAccessorConstant)
    }

    private fun String.forceCastIfMandatory(mandatory: Boolean): String {
        if (mandatory) {
            return "$this!!"
        }
        return this
    }
}
