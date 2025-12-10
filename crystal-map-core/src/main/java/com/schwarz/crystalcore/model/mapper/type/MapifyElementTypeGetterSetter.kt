package com.schwarz.crystalcore.model.mapper.type

import com.schwarz.crystalcore.javaToKotlinType
import com.schwarz.crystalcore.model.mapper.GetterSetter
import com.schwarz.crystalcore.model.source.ISourceDeclaringName
import com.schwarz.crystalcore.util.TypeUtil
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import java.lang.reflect.Method

class MapifyElementTypeGetterSetter<T>(
    val getterSetter: GetterSetter<T>,
    override val fieldName: String,
) : MapifyElementType<T> {
    override val elements: List<T> =
        listOfNotNull(getterSetter.getterElement?.source, getterSetter.setterElement?.source)

    override val mapName =
        getterSetter.mapify?.name?.let { if (it.isNotBlank()) it else null } ?: fieldName

    override val typeName = getterSetter.setterElement!!.typeName

    override val accessible =
        getterSetter.let {
            it.getterElement?.accessible == true && it.setterElement?.accessible == true
        } ?: false

    override val declaringName: ISourceDeclaringName =
        getterSetter.setterElement!!.asDeclaringName(
            getterSetter.mapify!!.nullableIndexes.toTypedArray(),
        )

    override fun reflectionProperties(sourceClazzTypeName: TypeName): List<PropertySpec> =
        listOf(
            PropertySpec
                .builder(
                    getterSetter.getterInternalAccessor(),
                    Method::class.java.asTypeName(),
                    KModifier.PRIVATE,
                ).initializer(
                    CodeBlock
                        .builder()
                        .addStatement(
                            "%T::class.java.getDeclaredMethod(%S)",
                            sourceClazzTypeName,
                            getterSetter.getterName(),
                        ).beginControlFlow(".apply")
                        .addStatement("isAccessible·=·true")
                        .endControlFlow()
                        .build(),
                ).build(),
            PropertySpec
                .builder(
                    getterSetter.setterInternalAccessor(),
                    Method::class.java.asTypeName(),
                    KModifier.PRIVATE,
                ).initializer(
                    CodeBlock
                        .builder()
                        .addStatement(
                            "%T::class.java.getDeclaredMethod(%S, %T::class.java)",
                            sourceClazzTypeName,
                            getterSetter.setterName(),
                            if (declaringName.isTypeVar()) TypeUtil.any().javaToKotlinType() else typeName,
                        ).beginControlFlow(".apply")
                        .addStatement("isAccessible·=·true")
                        .endControlFlow()
                        .build(),
                ).build(),
        )

    override fun getterFunSpec(): FunSpec =
        FunSpec
            .getterBuilder()
            .addStatement(
                "return %N.invoke(this) as? %T",
                getterSetter.getterInternalAccessor(),
                declaringName.asFullTypeName()!!.copy(nullable = true),
            ).build()

    override fun setterFunSpec(): FunSpec =
        FunSpec
            .setterBuilder()
            .addParameter(
                "value",
                declaringName.asFullTypeName()!!,
            ).addStatement("%N.invoke(this,·value)", getterSetter.setterInternalAccessor())
            .build()
}
