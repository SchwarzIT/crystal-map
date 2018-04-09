package com.kaufland.generation;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.couchbase.lite.UnsavedRevision;
import com.kaufland.model.entity.CblBaseEntityHolder;
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
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.Arrays;
import java.util.List;

import javax.lang.model.element.Modifier;

import kaufland.com.coachbasebinderapi.PersistenceConfig;

public class EntityGeneration {


    public JavaFile generateModel(CblEntityHolder holder) {

        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(holder.getEntitySimpleName()).
                addModifiers(Modifier.PUBLIC).
                addField(CblDefaultGeneration.field()).
                addField(TypeUtil.createMapStringObject(), "mDoc", Modifier.PRIVATE).
                addField(TypeUtil.createMapStringObject(), "mDocChanges", Modifier.PRIVATE).
                addMethods(create(holder)).
                addMethod(contructor(holder)).
                addMethod(getId()).
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

    private MethodSpec delete(CblEntityHolder holder) {
        return MethodSpec.methodBuilder("delete").addModifiers(Modifier.PUBLIC).
                addException(CouchbaseLiteException.class).
                addStatement("$N.getInstance().createOrGet(getId(), $S).delete()", PersistenceConfig.class.getCanonicalName(), holder.getDbName()).
                build();
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
                addStatement("temp.putAll(mDocDefaults)").
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

}
