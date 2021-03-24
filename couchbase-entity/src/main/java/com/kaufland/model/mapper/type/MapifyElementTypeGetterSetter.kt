package com.kaufland.model.mapper.type

import com.kaufland.ProcessingContext
import com.kaufland.javaToKotlinType
import com.kaufland.util.TypeUtil
import com.squareup.kotlinpoet.*
import com.sun.tools.javac.code.Symbol
import kaufland.com.coachbasebinderapi.mapify.Mapify
import java.lang.reflect.Method
import javax.lang.model.element.Modifier

class MapifyElementTypeGetterSetter(val getterSetter: GetterSetter, override val fieldName : String) : MapifyElementType {

    class GetterSetter(){
        var getterElement: Symbol.MethodSymbol?=null
        var setterElement: Symbol.MethodSymbol?=null
        var mapify: Mapify? = null

        fun getterName() : String{
            return getterElement!!.name.toString()
        }

        fun getterInternalAccessor() : String{
            return "a${getterName().capitalize()}"
        }

        fun setterName() : String{
            return setterElement!!.name.toString()
        }

        fun setterInternalAccessor() : String{
            return "a${setterName().capitalize()}"
        }
    }

    override val mapName = getterSetter.mapify?.name?.let { if(it.isNotBlank()) it else null } ?: fieldName

    override val typeName = getterSetter.setterElement!!.params()[0]!!.asType().asTypeName().javaToKotlinType()

    override val accessible = getterSetter?.let {
        it.getterElement?.modifiers?.contains(Modifier.PUBLIC) == true && it.setterElement?.modifiers?.contains(Modifier.PUBLIC) == true
    } ?: false

    override val declaringName: ProcessingContext.DeclaringName = ProcessingContext.DeclaringName(getterSetter.setterElement!!.params()[0]!!.asType(), 0, getterSetter.mapify!!.nullableIndexes.toTypedArray())

    override fun reflectionProperties(sourceClazzTypeName: TypeName): List<PropertySpec> {
        return listOf(PropertySpec.builder(getterSetter.getterInternalAccessor(), Method::class.java.asTypeName(), KModifier.PRIVATE)
                .initializer(CodeBlock.builder()
                        .addStatement("%T::class.java.getDeclaredMethod(%S)", sourceClazzTypeName, getterSetter.getterName())
                        .beginControlFlow(".apply")
                        .addStatement("isAccessible·=·true")
                        .endControlFlow().build()).build(),
                PropertySpec.builder(getterSetter.setterInternalAccessor(), Method::class.java.asTypeName(), KModifier.PRIVATE)
                        .initializer(CodeBlock.builder()
                                .addStatement("%T::class.java.getDeclaredMethod(%S, %T::class.java)", sourceClazzTypeName, getterSetter.setterName(), if(declaringName.isTypeVar()) TypeUtil.any().javaToKotlinType() else typeName)
                                .beginControlFlow(".apply")
                                .addStatement("isAccessible·=·true")
                                .endControlFlow().build()).build())
    }

    override fun getterFunSpec(): FunSpec {
        return FunSpec.getterBuilder().addStatement("return %N.invoke(this) as? %T", getterSetter.getterInternalAccessor(), declaringName.asFullTypeName()!!.copy(nullable = true)).build()
    }

    override fun setterFunSpec(): FunSpec {
        return FunSpec.setterBuilder().addParameter("value", declaringName.asFullTypeName()!!).addStatement("%N.invoke(this,·value)", getterSetter.setterInternalAccessor()).build()
    }
}