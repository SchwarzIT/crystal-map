package com.kaufland.generation

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName

import java.util.Arrays

import kaufland.com.coachbasebinderapi.PersistenceConfig
import kaufland.com.coachbasebinderapi.TypeConversion

class TypeConversionMethodsGeneration {

    fun generate(): Collection<FunSpec> {

        return Arrays.asList(
                FunSpec.builder(READ_METHOD_NAME).addParameter("value", Any::class.java).addParameter( "clazz", Class::class.java).addTypeVariable(TypeVariableName.invoke("T")).returns(TypeVariableName.invoke("T")).addCode(CodeBlock.builder().addStatement("\$N conversion = \$N.$GET_TYPE_CONVERSION_METHOD().get(clazz)", TypeConversion::class.java.canonicalName, PersistenceConfig::class.java.canonicalName).beginControlFlow("if(conversion == null)").addStatement("return (T) value").endControlFlow().addStatement("return (T) conversion.read(value)").build()).build(),

                FunSpec.builder(WRITE_METHOD_NAME).addParameter("value", Any::class.java).addParameter( "clazz", Class::class.java).addTypeVariable(TypeVariableName.invoke("T")).returns(TypeVariableName.invoke("T")).addCode(CodeBlock.builder().addStatement("\$N conversion = \$N.$GET_TYPE_CONVERSION_METHOD().get(clazz)", TypeConversion::class.java.canonicalName, PersistenceConfig::class.java.canonicalName).beginControlFlow("if(conversion == null)").addStatement("return (T) value").endControlFlow().addStatement("return (T) conversion.write(value)").build()).build()

        )

    }

    companion object {

        val READ_METHOD_NAME = "read"

        val WRITE_METHOD_NAME = "write"

        private val GET_TYPE_CONVERSION_METHOD = "getInstance().getConnector().getTypeConversions"
    }
}
