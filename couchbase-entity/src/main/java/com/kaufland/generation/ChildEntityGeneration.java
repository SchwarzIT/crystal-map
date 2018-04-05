package com.kaufland.generation;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.couchbase.lite.UnsavedRevision;
import com.kaufland.model.entity.CblBaseEntityHolder;
import com.kaufland.model.entity.CblChildEntityHolder;
import com.kaufland.model.entity.CblEntityHolder;
import com.kaufland.model.field.CblAttachmentFieldHolder;
import com.kaufland.model.field.CblBaseFieldHolder;
import com.kaufland.model.field.CblConstantHolder;
import com.kaufland.model.field.CblFieldHolder;
import com.kaufland.util.TypeUtil;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.lang.model.element.Modifier;

import kaufland.com.coachbasebinderapi.PersistenceConfig;

public class ChildEntityGeneration {
    
    public JavaFile generateModel(CblChildEntityHolder holder) {

        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(holder.getEntitySimpleName()).
                addModifiers(Modifier.PUBLIC).
                addMethods(create(holder)).
                addField(TypeUtil.createMapStringObject(), "mDoc", Modifier.PRIVATE).
                addMethod(contructor()).
                superclass(TypeName.get(holder.getSourceElement().asType()));

        for (CblBaseFieldHolder fieldHolder : holder.getAllFields()) {

            typeBuilder.addFields(fieldHolder.createFieldConstant());
            typeBuilder.addMethod(fieldHolder.getter(null));

            MethodSpec setter = fieldHolder.setter(null, holder.getEntityTypeName(), false);
            if (setter != null) {
                typeBuilder.addMethod(setter);
            }
        }


        typeBuilder.addMethod(new RebindMethodGeneration().generate(holder.getFields(), false));
        typeBuilder.addMethods(fromMap(holder));
        typeBuilder.addMethods(toMap(holder));

        return JavaFile.builder(holder.getPackage(), typeBuilder.build()).
                build();

    }

    private List<MethodSpec> toMap(CblBaseEntityHolder holder) {
        CodeBlock nullCheck = CodeBlock.builder().
                beginControlFlow("if(obj == null)").
                addStatement("return null").
                endControlFlow().
                build();

        return Arrays.asList(MethodSpec.methodBuilder("toMap").addModifiers(Modifier.PUBLIC, Modifier.STATIC).
                        addParameter(holder.getEntityTypeName(), "obj").
                        returns(TypeUtil.createMapStringObject()).
                        addCode(nullCheck).
                        addStatement("$T result = new $T()", TypeUtil.createHashMapStringObject(), TypeUtil.createHashMapStringObject()).
                        addStatement("result.putAll(obj.mDoc)").
                        addStatement("return result").
                        build(),

                MethodSpec.methodBuilder("toMap").addModifiers(Modifier.PUBLIC, Modifier.STATIC).
                        addParameter(ParameterizedTypeName.get(ClassName.get(List.class), holder.getEntityTypeName()), "obj").
                        returns(TypeUtil.createListWithMapStringObject()).
                        addCode(nullCheck).
                        addStatement("$T result = new $T()", TypeUtil.createListWithMapStringObject(), TypeUtil.createArrayListWithMapStringObject()).
                        addCode(CodeBlock.builder().beginControlFlow("for($N entry : obj)", holder.getEntitySimpleName()).
                                addStatement("result.add((($N)entry).toMap(entry))", holder.getEntitySimpleName()).
                                endControlFlow().
                                build()).
                        addStatement("return result").
                        build());
    }

    private List<MethodSpec> fromMap(CblBaseEntityHolder holder) {
        CodeBlock nullCheck = CodeBlock.builder().
                beginControlFlow("if(obj == null)").
                addStatement("return null").
                endControlFlow().
                build();

        return Arrays.asList(MethodSpec.methodBuilder("fromMap").addModifiers(Modifier.PUBLIC, Modifier.STATIC).
                        addParameter(TypeUtil.createMapStringObject(), "obj").
                        returns(holder.getEntityTypeName()).
                        addCode(nullCheck).
                        addStatement("return new $T(obj)", holder.getEntityTypeName()).
                        build(),

                MethodSpec.methodBuilder("fromMap").addModifiers(Modifier.PUBLIC, Modifier.STATIC).
                        addParameter(ParameterizedTypeName.get(ClassName.get(List.class), TypeUtil.createMapStringObject()), "obj").
                        returns(ParameterizedTypeName.get(ClassName.get(List.class), holder.getEntityTypeName())).
                        addCode(nullCheck).
                        addStatement("$T result = new $T()", ParameterizedTypeName.get(ClassName.get(List.class), holder.getEntityTypeName()), ParameterizedTypeName.get(ClassName.get(ArrayList.class), holder.getEntityTypeName())).
                        addCode(CodeBlock.builder().beginControlFlow("for($T entry : obj)", TypeUtil.createMapStringObject()).
                                addStatement("result.add(new $N(entry))", holder.getEntitySimpleName()).
                                endControlFlow().
                                build()).
                        addStatement("return result").
                        build()
        );
    }

    private MethodSpec contructor() {
        return MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC).
                addParameter(TypeUtil.createMapStringObject(), "doc").
                addStatement("rebind(doc)").
                build();
    }

    private List<MethodSpec> create(CblChildEntityHolder holder) {

        return Arrays.asList(
                MethodSpec.methodBuilder("create").
                        addModifiers(Modifier.PUBLIC, Modifier.STATIC).
                        addParameter(TypeUtil.createMapStringObject(), "doc").
                        addStatement("return new $N (doc)",
                                holder.getEntitySimpleName()).
                        returns(holder.getEntityTypeName()).
                        build(),
                MethodSpec.methodBuilder("create").
                        addModifiers(Modifier.PUBLIC, Modifier.STATIC).
                        addStatement("return new $N (new $T())",
                                holder.getEntitySimpleName(), TypeUtil.createHashMapStringObject()).
                        returns(holder.getEntityTypeName()).
                        build()
        );
    }

}
