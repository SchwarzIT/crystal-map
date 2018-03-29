package com.kaufland.model.source;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.kaufland.util.TypeUtil;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.thoughtworks.qdox.model.JavaField;

import org.apache.commons.lang3.text.WordUtils;

import java.util.Arrays;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

import kaufland.com.coachbasebinderapi.CblField;
import kaufland.com.coachbasebinderapi.PersistenceConfig;

public class CblAttachmentFieldHolder extends CblBaseFieldHolder {

    private String mAttachmentType;

    public CblAttachmentFieldHolder(CblField field, Element fieldElement, JavaField metaField) {
        super(field.value(), fieldElement, metaField);
        mAttachmentType = field.attachmentType();
    }

    public String getAttachmentType() {
        return mAttachmentType;
    }

    @Override
    public MethodSpec getter(String dbName) {
        TypeName returnType = TypeUtil.parseMetaType(getMetaField().getType(), null);

        MethodSpec.Builder builder = MethodSpec.methodBuilder("get" + WordUtils.capitalize(getMetaField().getName())).
                addModifiers(Modifier.PUBLIC).
                returns(returnType).
                addException(CouchbaseLiteException.class).
                addCode(CodeBlock.builder().
                        addStatement("$T doc = $N.getInstance().createOrGet(getId(), $S)", ClassName.get(Document.class), PersistenceConfig.class.getCanonicalName(), dbName).
                        beginControlFlow("if(doc.getCurrentRevision() != null && doc.getCurrentRevision().getAttachments() != null &&  doc.getCurrentRevision().getAttachments().size() > 0)").
                        addStatement("return doc.getCurrentRevision().getAttachments().get(0).getContent()").
                        endControlFlow().
                        addStatement("return null").build());
        return builder.build();
    }

    @Override
    public MethodSpec setter(String dbName, TypeName entityTypeName) {

        TypeName fieldType = TypeUtil.parseMetaType(getMetaField().getType(), null);
        MethodSpec.Builder builder = MethodSpec.methodBuilder("set" + WordUtils.capitalize(getMetaField().getName())).
                addModifiers(Modifier.PUBLIC).
                addParameter(fieldType, "value").
                returns(entityTypeName).
                addStatement("$N = value", getDbField()).
                addStatement("return this");
        return builder.build();
    }

    @Override
    public List<FieldSpec> createFieldConstant() {

        FieldSpec fieldAccessorConstant = FieldSpec.builder(String.class, getConstantName(), Modifier.FINAL, Modifier.PUBLIC, Modifier.STATIC).
                initializer("$S", getDbField()).
                build();

        return Arrays.asList(fieldAccessorConstant,
                FieldSpec.builder(TypeUtil.parseMetaType(getMetaField().getType()), getDbField(), Modifier.PRIVATE).
                        build());
    }
}
