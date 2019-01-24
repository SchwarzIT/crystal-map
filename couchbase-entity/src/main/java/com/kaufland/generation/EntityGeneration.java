package com.kaufland.generation;

import com.kaufland.model.entity.EntityHolder;
import com.kaufland.model.field.CblBaseFieldHolder;
import com.kaufland.model.field.CblConstantHolder;
import com.kaufland.util.TypeUtil;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.Arrays;
import java.util.List;

import javax.lang.model.element.Modifier;

import kaufland.com.coachbasebinderapi.PersistenceConfig;
import kaufland.com.coachbasebinderapi.PersistenceException;

public class EntityGeneration {

    private static final String GET_DOCUMENT_METHOD = "getInstance().getConnector().getDocument";

    private static final String DELETE_DOCUMENT_METHOD = "getInstance().getConnector().deleteDocument";

    private static final String UPSERT_DOCUMENT_METHOD = "getInstance().getConnector().upsertDocument";


    public JavaFile generateModel(EntityHolder holder) {

        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(holder.getEntitySimpleName()).
                addModifiers(Modifier.PUBLIC).
                addField(CblDefaultGeneration.field()).
                addField(TypeUtil.createMapStringObject(), "mDoc", Modifier.PRIVATE).
                addField(TypeUtil.createMapStringObject(), "mDocChanges", Modifier.PRIVATE).
                addMethods(create(holder)).
                addMethod(contructor(holder)).
                addMethod(setAll(holder)).
                addMethods(new TypeConversionMethodsGeneration().generate()).
                addMethod(getId()).
                addMethod(toMap(holder)).
                addField(idConstant()).
                superclass(TypeName.get(holder.getSourceElement().asType()));


        for (CblBaseFieldHolder fieldHolder : holder.getAllFields()) {

            typeBuilder.addFields(fieldHolder.createFieldConstant());
            typeBuilder.addMethod(fieldHolder.getter(holder.getDbName(), true));

            MethodSpec setter = fieldHolder.setter(holder.getDbName(), holder.getEntityTypeName(), true);
            if (setter != null) {
                typeBuilder.addMethod(setter);
            }
        }

        typeBuilder.addStaticBlock(CblDefaultGeneration.staticInitialiser(holder));
        typeBuilder.addMethod(new RebindMethodGeneration().generate(true));
        typeBuilder.addMethod(delete(holder));
        typeBuilder.addMethod(save(holder));

        return JavaFile.builder(holder.getPackage(), typeBuilder.build()).
                build();

    }


    private FieldSpec idConstant() {
        return FieldSpec.builder(String.class, "_ID", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL).
                initializer("$S", "_id").
                build();
    }

    private MethodSpec getId() {

        return MethodSpec.methodBuilder("getId").
                addModifiers(Modifier.PUBLIC).
                returns(String.class).
                addStatement("return ($T) mDoc.get($N)", String.class, "_ID").
                build();
    }



    private MethodSpec setAll(EntityHolder holder) {

        MethodSpec.Builder setAllBuilder = MethodSpec.methodBuilder("setAll").addModifiers(Modifier.PUBLIC).
                addParameter(TypeUtil.createMapStringObject(), "map").
                addStatement("mDocChanges.putAll(map)", TypeUtil.createMapStringObject(), PersistenceConfig.class, holder.getDbName());

        return setAllBuilder.build();
    }

    private MethodSpec toMap(EntityHolder holder) {

        MethodSpec.Builder toMapBuilder = MethodSpec.methodBuilder("toMap").addModifiers(Modifier.PUBLIC).
                returns(TypeUtil.createMapStringObject()).
                addStatement("$T doc = $T." + GET_DOCUMENT_METHOD + "(getId(), $S)", TypeUtil.createMapStringObject(), PersistenceConfig.class, holder.getDbName());

        for (CblConstantHolder constantField : holder.getFieldConstants()) {
            toMapBuilder.addStatement("mDocChanges.put($S, $S)", constantField.getDbField(), constantField.getConstantValue());
        }

        toMapBuilder.addStatement("$1T temp = new $1T()", TypeUtil.createHashMapStringObject());
        toMapBuilder.addCode(CodeBlock.builder().
                addStatement("temp.putAll(mDocDefaults)").
                beginControlFlow("if(doc != null)").
                addStatement("temp.putAll(doc)").
                endControlFlow().
                beginControlFlow("if(mDocChanges != null)").
                addStatement("temp.putAll(mDocChanges)").
                endControlFlow().
                addStatement("return temp").
                build());

        return toMapBuilder.build();
    }

    private MethodSpec delete(EntityHolder holder) {
        return MethodSpec.methodBuilder("delete").addModifiers(Modifier.PUBLIC).
                addException(PersistenceException.class).
                addStatement("$N." + DELETE_DOCUMENT_METHOD + "(getId(), $S)", PersistenceConfig.class.getCanonicalName(), holder.getDbName()).
                build();
    }

    private MethodSpec save(EntityHolder holder) {
        MethodSpec.Builder saveBuilder = MethodSpec.methodBuilder("save").addModifiers(Modifier.PUBLIC).
                addException(PersistenceException.class).
                addStatement("$T doc = toMap()", TypeUtil.createMapStringObject());

        saveBuilder.addStatement("$N." + UPSERT_DOCUMENT_METHOD + "(doc, getId(), $S)", PersistenceConfig.class.getCanonicalName(), holder.getDbName());
        saveBuilder.addStatement("rebind(doc)");

        return saveBuilder.build();
    }

    private MethodSpec contructor(EntityHolder holder) {
        return MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC).
                addParameter(TypeUtil.createMapStringObject(), "doc").
                addStatement("rebind(doc)").
                build();
    }

    private List<MethodSpec> create(EntityHolder holder) {

        return Arrays.asList(
                MethodSpec.methodBuilder("create").
                        addModifiers(Modifier.PUBLIC, Modifier.STATIC).
                        addParameter(String.class, "id").
                        addStatement("return new $N ($N." + GET_DOCUMENT_METHOD + "(id, $S))",
                                holder.getEntitySimpleName(), PersistenceConfig.class.getCanonicalName(), holder.getDbName()).
                        returns(holder.getEntityTypeName()).
                        build(),
                MethodSpec.methodBuilder("create").
                        addModifiers(Modifier.PUBLIC, Modifier.STATIC).
                        addStatement("return new $N ($N." + GET_DOCUMENT_METHOD + "(null, $S))",
                                holder.getEntitySimpleName(), PersistenceConfig.class.getCanonicalName(), holder.getDbName()).
                        returns(holder.getEntityTypeName()).
                        build(),
                MethodSpec.methodBuilder("create").
                        addModifiers(Modifier.PUBLIC, Modifier.STATIC).
                        addParameter(TypeUtil.createMapStringObject(), "map").
                        addStatement("return new $N (map)",
                                holder.getEntitySimpleName()).
                        returns(holder.getEntityTypeName()).
                        build()
        );
    }

}
