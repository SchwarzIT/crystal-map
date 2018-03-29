package com.kaufland.model;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.couchbase.lite.UnsavedRevision;
import com.kaufland.model.source.CblAttachmentFieldHolder;
import com.kaufland.model.source.CblBaseFieldHolder;
import com.kaufland.model.source.CblConstantHolder;
import com.kaufland.model.source.CblEntityHolder;
import com.kaufland.model.source.CblFieldHolder;
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

public class EntityGeneration implements GenerationModel {


    @Override
    public JavaFile generateModel(CblEntityHolder holder) {

        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(holder.getEntitySimpleName()).
                addModifiers(Modifier.PUBLIC).
                addField(TypeUtil.createMapStringObject(), "mDoc", Modifier.PRIVATE).
                addField(TypeUtil.createMapStringObject(), "mDocChanges", Modifier.PRIVATE).
                addMethods(create(holder)).
                addMethod(contructor(holder)).
                addMethod(getId()).
                addField(idConstant()).
                superclass(TypeName.get(holder.getSourceElement().asType()));

        MethodSpec.Builder rebind = MethodSpec.methodBuilder("rebind").
                addParameter(TypeUtil.createMapStringObject(), "doc").
                addStatement("mDoc = doc != null ? doc : new java.util.HashMap<String, Object>()").
                addStatement("mDocChanges = new java.util.HashMap<String, Object>()").
                addStatement("$T mDocDefaults = new $T()", TypeUtil.createHashMapStringObject(), TypeUtil.createHashMapStringObject());


        for (CblBaseFieldHolder fieldHolder : holder.getAllFields()) {

            typeBuilder.addFields(fieldHolder.createFieldConstant());
            typeBuilder.addMethod(fieldHolder.getter(holder.getDbName()));

            MethodSpec setter = fieldHolder.setter(holder.getDbName(), holder.getEntityTypeName());
            if (setter != null) {
                typeBuilder.addMethod(setter);
            }
        }

        for (CblFieldHolder fieldHolder : holder.getFields()) {
            if (fieldHolder.getDefaultHolder() != null) {

                rebind.addCode(CodeBlock.builder().
                        beginControlFlow("if(!mDoc.containsKey($N))", fieldHolder.getConstantName()).
                        addStatement("mDocDefaults.put($N, $N)", fieldHolder.getConstantName(), convertDefaultValue(fieldHolder)).
                        endControlFlow().
                        build());
            }
        }

        rebind.addCode(CodeBlock.builder().
                beginControlFlow("if(mDocDefaults.size()>0)").
                addStatement("mDoc.putAll(mDocDefaults)").
                endControlFlow().
                build());


        typeBuilder.addMethod(rebind.build());
        typeBuilder.addMethod(delete(holder));
        typeBuilder.addMethods(fromMap(holder));
        typeBuilder.addMethods(toMap(holder));
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

    private MethodSpec delete(CblEntityHolder holder) {
        return MethodSpec.methodBuilder("delete").addModifiers(Modifier.PUBLIC).
                addException(CouchbaseLiteException.class).
                addStatement("$N.getInstance().createOrGet(getId(), $S).delete()", PersistenceConfig.class.getCanonicalName(), holder.getDbName()).
                build();
    }

    private List<MethodSpec> toMap(CblEntityHolder holder) {
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
                        addStatement("result.putAll(obj.mDocChanges)").
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

    private List<MethodSpec> fromMap(CblEntityHolder holder) {
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

    private MethodSpec save(CblEntityHolder holder) {
        MethodSpec.Builder saveBuilder = MethodSpec.methodBuilder("save").addModifiers(Modifier.PUBLIC).
                addException(CouchbaseLiteException.class).
                addStatement("$T doc = $T.getInstance().createOrGet(getId(), $S)", Document.class, PersistenceConfig.class, holder.getDbName());

        for (CblConstantHolder constantField : holder.getFieldConstants()) {
            saveBuilder.addStatement("mDocChanges.put($S, $S)", constantField.getDbField(), constantField.getConstantValue());
        }

        saveBuilder.addStatement("$1T temp = new $1T()", TypeUtil.createHashMapStringObject());
        saveBuilder.addCode(CodeBlock.builder().
                beginControlFlow("if(doc.getProperties() != null)").
                addStatement("temp.putAll(doc.getProperties())").
                endControlFlow().
                beginControlFlow("if(mDocChanges != null)").
                addStatement("temp.putAll(mDocChanges)").
                endControlFlow().
                addStatement("doc.putProperties(temp)").
                build());


        if (!holder.getFieldAttachments().isEmpty()) {
            saveBuilder.addStatement("$T rev = doc.createRevision()", ClassName.get(UnsavedRevision.class));
            for (CblAttachmentFieldHolder attachmentField : holder.getFieldAttachments()) {

                saveBuilder.addCode(CodeBlock.builder().beginControlFlow("if($N != null)", attachmentField.getDbField()).
                        addStatement("rev.setAttachment($N, $S, $N)", attachmentField.getConstantName(), attachmentField.getAttachmentType(), attachmentField.getDbField()).
                        endControlFlow().build());
                saveBuilder.addStatement("rev.save()");
            }
        }

        saveBuilder.addStatement("rebind(doc.getProperties())");

        return saveBuilder.build();
    }

    private MethodSpec contructor(CblEntityHolder holder) {
        return MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC).
                addParameter(TypeUtil.createMapStringObject(), "doc").
                addStatement("rebind(doc)").
                build();
    }

    private List<MethodSpec> create(CblEntityHolder holder) {

        return Arrays.asList(
                MethodSpec.methodBuilder("create").
                        addModifiers(Modifier.PUBLIC, Modifier.STATIC).
                        addParameter(String.class, "id").
                        addStatement("return new $N ($N.getInstance().createOrGet(id, $S).getProperties())",
                                holder.getEntitySimpleName(), PersistenceConfig.class.getCanonicalName(), holder.getDbName()).
                        returns(holder.getEntityTypeName()).
                        build(),
                MethodSpec.methodBuilder("create").
                        addModifiers(Modifier.PUBLIC, Modifier.STATIC).
                        addStatement("return new $N ($N.getInstance().createOrGet(null, $S).getProperties())",
                                holder.getEntitySimpleName(), PersistenceConfig.class.getCanonicalName(), holder.getDbName()).
                        returns(holder.getEntityTypeName()).
                        build()
        );
    }

    private String convertDefaultValue(CblFieldHolder fieldHolder) {

        if (fieldHolder.getMetaField().getType().getCanonicalName().equals(String.class.getCanonicalName())) {
            return "\"" + fieldHolder.getDefaultHolder().getDefaultValue() + "\"";
        }
        return fieldHolder.getDefaultHolder().getDefaultValue();
    }

}
