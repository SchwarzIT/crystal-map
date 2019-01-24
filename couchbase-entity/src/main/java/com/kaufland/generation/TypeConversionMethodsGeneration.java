package com.kaufland.generation;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;

import java.util.Arrays;
import java.util.Collection;

import kaufland.com.coachbasebinderapi.PersistenceConfig;
import kaufland.com.coachbasebinderapi.TypeConversion;

public class TypeConversionMethodsGeneration {

    public static final String READ_METHOD_NAME = "read";

    public static final String WRITE_METHOD_NAME = "write";

    private static final String GET_TYPE_CONVERSION_METHOD = "getInstance().getConnector().getTypeConversions";

    public Collection<MethodSpec> generate() {

        return Arrays.asList(
                MethodSpec.methodBuilder(READ_METHOD_NAME).
                addParameter(TypeName.OBJECT, "value").
                addParameter(Class.class, "clazz").
                addTypeVariable(TypeVariableName.get("T")).
                returns(TypeVariableName.get("T")).
                addCode(CodeBlock.builder().
                        addStatement("$N conversion = $N." + GET_TYPE_CONVERSION_METHOD + "().get(clazz)", TypeConversion.class.getCanonicalName(), PersistenceConfig.class.getCanonicalName()).
                        beginControlFlow("if(conversion == null)").
                        addStatement("return (T) value").
                        endControlFlow().
                        addStatement("return (T) conversion.read(value)").
                        build()).build(),

                MethodSpec.methodBuilder(WRITE_METHOD_NAME).
                        addParameter(TypeName.OBJECT, "value").
                        addParameter(Class.class, "clazz").
                        addTypeVariable(TypeVariableName.get("T")).
                        returns(TypeVariableName.get("T")).
                        addCode(CodeBlock.builder().
                                addStatement("$N conversion = $N." + GET_TYPE_CONVERSION_METHOD + "().get(clazz)", TypeConversion.class.getCanonicalName(), PersistenceConfig.class.getCanonicalName()).
                                beginControlFlow("if(conversion == null)").
                                addStatement("return (T) value").
                                endControlFlow().
                                addStatement("return (T) conversion.write(value)").
                                build()).build()

        );

    }
}
