package com.kaufland.generation

import com.kaufland.util.TypeUtil
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeVariableName

import java.util.Arrays

import kaufland.com.coachbasebinderapi.PersistenceConfig

class TypeConversionMethodsGeneration {

    fun generate(): Collection<FunSpec> {

        return Arrays.asList(
                FunSpec.builder(READ_METHOD_NAME).
                        addParameter("value", TypeUtil.any().copy(nullable = true)).
                        addParameter( "clazz", TypeUtil.classStar()).addTypeVariable(TypeVariableName.invoke("T")).
                        returns(TypeVariableName.invoke("T?")).addCode(CodeBlock.builder().
                        addStatement("val conversion = %T.$GET_TYPE_CONVERSION_METHOD().get(clazz)", PersistenceConfig::class).
                        beginControlFlow("if(conversion == null)").
                        addStatement("return value as T").
                        endControlFlow().
                        addStatement("return conversion?.read(value) as T").build()).build(),

                FunSpec.builder(WRITE_METHOD_NAME).addParameter("value", TypeUtil.any().copy(nullable = true)).
                        addParameter( "clazz", TypeUtil.classStar()).addTypeVariable(TypeVariableName.invoke("T")).
                        returns(TypeVariableName.invoke("T?")).
                        addCode(CodeBlock.builder().
                                addStatement("val conversion = %T.$GET_TYPE_CONVERSION_METHOD().get(clazz)", PersistenceConfig::class).
                                beginControlFlow("if(conversion == null)").addStatement("return value as T").
                                endControlFlow().addStatement("return conversion.write(value) as T").build()).build()

        )

    }

    companion object {

        val READ_METHOD_NAME = "read"

        val WRITE_METHOD_NAME = "write"

        private val GET_TYPE_CONVERSION_METHOD = "getInstance().getConnector().getTypeConversions"
    }
}
