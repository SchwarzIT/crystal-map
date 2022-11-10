package com.schwarz.crystalprocessor.model.mapper.type

import com.schwarz.crystalprocessor.ProcessingContext
import com.schwarz.crystalprocessor.ProcessingContext.asDeclaringName
import com.schwarz.crystalprocessor.javaToKotlinType
import com.squareup.kotlinpoet.*
import com.schwarz.crystalapi.mapify.Mapify
import java.lang.reflect.Field
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier

class MapifyElementTypeField(val element: Element, val mapify: Mapify) : MapifyElementType {
    override val elements: Array<Element> = arrayOf(element)

    override val fieldName = element.simpleName.toString()

    override val mapName = if (mapify.name.isNotBlank()) mapify.name else fieldName

    override val typeName = element.asType().asTypeName().javaToKotlinType()

    override val accessible = element.modifiers.contains(Modifier.PUBLIC)

    override val declaringName: ProcessingContext.DeclaringName = element.asDeclaringName(mapify.nullableIndexes.toTypedArray())

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
