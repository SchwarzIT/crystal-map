package com.kaufland.model.accessor

import com.kaufland.javaToKotlinType
import com.kaufland.model.source.SourceMemberField
import com.kaufland.model.source.SourceMemberFunction
import com.squareup.kotlinpoet.*
import org.jetbrains.annotations.Nullable
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeMirror
import kotlin.coroutines.Continuation

class CblGenerateAccessorHolder(
    private val sourceClassTypeName: TypeName,
    private val memberFunction: SourceMemberFunction?,
    private val memberProperty: SourceMemberField?
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

            methodBuilder.addCode(
                CodeBlock.of(
                    "return %T.%N(${callParams.joinToString()})" + System.lineSeparator(),
                    sourceClassTypeName,
                    memberFunction.name
                )
            )
            return methodBuilder.build()
        }

        return null
    }

    private fun isSuspendFunction(varElement: VariableElement): Boolean {
        return varElement.asType().toString().contains(Continuation::class.qualifiedName.toString())
    }

    private fun evaluateTypeName(typeMirror: TypeMirror, nullable: Boolean): TypeName {
        return typeMirror.asTypeName().javaToKotlinType().copy(nullable = nullable)
    }

    fun accessorPropertySpec(): PropertySpec? {

        if(memberProperty != null){
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
