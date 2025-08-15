package com.schwarz.crystalcore.model.mapper.type

import com.schwarz.crystalapi.mapify.Mapify
import com.schwarz.crystalcore.model.source.IClassModel
import com.schwarz.crystalcore.model.source.ISourceDeclaringName
import com.schwarz.crystalcore.model.source.ISourceMapify
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import java.lang.reflect.Field

class MapifyElementTypeField<T>(val element: IClassModel<T>, val mapify: ISourceMapify) :
    MapifyElementType<T> {

    override val elements: List<T> = listOf(
        element.source
    )

    override val fieldName = element.sourceClazzSimpleName

    override val mapName = if (mapify.name.isNotBlank()) mapify.name else fieldName

    override val typeName = element.typeName

    override val accessible = element.accessible

    override val declaringName: ISourceDeclaringName = element.asDeclaringName(mapify.nullableIndexes.toTypedArray())

    override fun reflectionProperties(sourceClazzTypeName: TypeName): List<PropertySpec> {
        return listOf(
            PropertySpec.builder(reflectedFieldName, Field::class.java.asTypeName(), KModifier.PRIVATE)
                .initializer(
                    CodeBlock.builder()
                        .addStatement("%T::class.java.getDeclaredField(%S)", sourceClazzTypeName, fieldName)
                        .beginControlFlow(".apply")
                        .addStatement("isAccessible·=·true")
                        .endControlFlow().build()
                ).build()
        )
    }

    override fun getterFunSpec(): FunSpec {
        return FunSpec.getterBuilder().addStatement("return %N.get(this) as? %T", reflectedFieldName, declaringName.asFullTypeName()!!.copy(nullable = true)).build()
    }

    override fun setterFunSpec(): FunSpec {
        return FunSpec.setterBuilder().addParameter("value", declaringName.asFullTypeName()!!).addStatement("%N.set(this,·value)", reflectedFieldName).build()
    }
}
