package com.schwarz.crystalprocessor.model.accessor

import com.schwarz.crystalprocessor.model.source.SourceMemberField
import com.schwarz.crystalprocessor.model.source.SourceMemberFunction
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName

class CblGenerateAccessorHolder(
    private val sourceClassTypeName: TypeName,
    private val memberFunction: SourceMemberFunction?,
    private val memberProperty: SourceMemberField?,
) {

    fun accessorFunSpec(): FunSpec? {

        if (memberFunction != null) {
            val methodBuilder = FunSpec.builder(memberFunction.name).addAnnotation(JvmStatic::class)

            if (memberFunction.isSuspend) {
                methodBuilder.addModifiers(KModifier.SUSPEND)
            }
            val callParams = arrayListOf<String>()
            memberFunction.parameters.forEach {
                callParams.add(it.name)
                methodBuilder.addParameter(it.name, it.type)
            }

            methodBuilder.addStatement(
                "return %T.%N(${callParams.joinToString()})" + System.lineSeparator(),
                sourceClassTypeName,
                memberFunction.name
            )
            
            return methodBuilder.build()
        }

        return null
    }

    fun accessorPropertySpec(): PropertySpec? {

        if (memberProperty != null) {
            return PropertySpec.builder(
                memberProperty.name,
                memberProperty.type
            ).addAnnotation(JvmField::class)
                .initializer("%T.%N", sourceClassTypeName, memberProperty.name)
                .build()
        }

        return null
    }
}
