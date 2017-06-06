package com.kaufland.model;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.couchbase.lite.UnsavedRevision;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.JClassAlreadyExistsException;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JFieldVar;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JPackage;
import com.kaufland.model.source.CblEntityHolder;
import com.kaufland.model.source.CblFieldHolder;

import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sbra0902 on 02.06.17.
 */

public class EntityGeneration implements GenerationModel {


    private final CblEntityHolder mHolder;

    public EntityGeneration(CblEntityHolder holder) {
        mHolder = holder;
    }

    @Override
    public JCodeModel generateModel(JCodeModel codeModel) throws JClassAlreadyExistsException {

        codeModel._ref(UnsavedRevision.class);
        codeModel._ref(Document.class);

        JPackage mJPackage = codeModel._package(mHolder.getSourceClazz()._package().name());

        JDefinedClass genClazz = mJPackage._class(mHolder.getSourceClazz().name() + "Entity");

        JMethod mUpsert = genClazz.method(JMod.PUBLIC | JMod.STATIC, genClazz, "create");
        mUpsert.param(String.class, "id");
        mUpsert.body().directStatement("return new " + genClazz.name() + "(kaufland.com.coachbasebinderapi.PersistenceConfig.getInstance().createOrGet(id).getProperties());");

        JMethod mCreate = genClazz.method(JMod.PUBLIC | JMod.STATIC, genClazz, "create");
        mCreate.body().directStatement("return new " + genClazz.name() + "(kaufland.com.coachbasebinderapi.PersistenceConfig.getInstance().createOrGet(null).getProperties());");

        AbstractJClass mapClazz = codeModel.directClass(Map.class.getCanonicalName()).narrow(String.class, Object.class);

        genClazz.field(JMod.PRIVATE, mapClazz, "mDoc");

        genClazz.field(JMod.PRIVATE, mapClazz, "mDocChanges");

        JMethod ctr = genClazz.constructor(JMod.PUBLIC);
        ctr.param(mapClazz, "doc");
        ctr.body().directStatement("mDoc = doc != null ? doc : new java.util.HashMap<String, Object>(); mDocChanges = new java.util.HashMap<String, Object>();");

        List<CblFieldHolder> attachmentFields = new ArrayList<>();

        for (CblFieldHolder fieldHolder : mHolder.getFields()) {

            JMethod setter = genClazz.method(JMod.PUBLIC, genClazz, "set" + WordUtils.capitalize(fieldHolder.getClazzFieldName().toString()));
            setter.param(fieldHolder.getType(), "value");

            JMethod getter = genClazz.method(JMod.PUBLIC, fieldHolder.getType(), "get" + WordUtils.capitalize(fieldHolder.getClazzFieldName()));

            if (fieldHolder.isTypeOfSubEntity()) {
                createSetterBodySubEntity(fieldHolder, setter);
                createGetterBodySubEntity(fieldHolder, getter);

            } else if (!fieldHolder.isAttachment()) {
                createSetterBodyDefault(fieldHolder, setter);
                createGetterBodyDefault(fieldHolder.getType(), fieldHolder.getDbField(), getter);
            } else {

                JFieldVar attachment = genClazz.field(JMod.PRIVATE, fieldHolder.getType(), fieldHolder.getDbField());
                setter.body().directStatement(fieldHolder.getDbField() + "= value; return this;");
                attachmentFields.add(fieldHolder);
                createGetterBodyDefault(fieldHolder.getType(), fieldHolder.getDbField(), getter);
            }

            createFieldConstant(genClazz, fieldHolder.getDbField());


        }

        createFromMap(codeModel, genClazz);
        createToMap(codeModel, genClazz);

        createSaveMethod(codeModel, genClazz, attachmentFields);

        createDeleteMethod(codeModel, genClazz);

        genClazz._extends(mHolder.getSourceClazz());

        return codeModel;


    }

    private void createSetterBodyDefault(CblFieldHolder fieldHolder, JMethod setter) {
        setter.body().directStatement("mDocChanges.put(" + fieldHolder.getDbField().toUpperCase() + ", value); return this;");
    }

    private void createGetterBodySubEntity(CblFieldHolder fieldHolder, JMethod getter) {
        StringBuilder builder = new StringBuilder();
        builder.append("return (" + fieldHolder.getType().fullName() + ") " + fieldHolder.getSubEntityName() + "Entity.fromMap((");
        if (fieldHolder.isSubEntityIsTypeParam()) {
            builder.append("java.util.List<java.util.HashMap<String, Object>>");
        } else {
            builder.append("java.util.HashMap<String, Object>");
        }
        builder.append(")mDoc.get(" + fieldHolder.getDbField().toUpperCase() + "));");

        getter.body().directStatement(builder.toString());
    }

    private void createSetterBodySubEntity(CblFieldHolder fieldHolder, JMethod setter) {
        StringBuilder builder = new StringBuilder();
        builder.append("mDocChanges.put(" + fieldHolder.getDbField().toUpperCase() + ", " + fieldHolder.getSubEntityName() + "Entity.toMap((");

        if (fieldHolder.isSubEntityIsTypeParam()) {
            builder.append("java.util.List<" + fieldHolder.getSubEntityName() + ">");
        } else {
            builder.append(fieldHolder.getSubEntityName());
        }

        builder.append(")value)); return this;");
        setter.body().directStatement(builder.toString());
    }

    private void createDeleteMethod(JCodeModel codeModel, JDefinedClass genClazz) {
        JMethod delete = genClazz.method(JMod.PUBLIC, codeModel.VOID, "delete");
        delete._throws(CouchbaseLiteException.class);
        delete.body().directStatement("kaufland.com.coachbasebinderapi.PersistenceConfig.getInstance().createOrGet(getId()).delete();");
    }

    private void createGetterBodyDefault(AbstractJClass resturnValue, String dbField, JMethod getter) {
        getter.body().directStatement("return (" + resturnValue.fullName() + ") mDoc.get(" + dbField.toUpperCase() + ");");
    }

    private void createToMap(JCodeModel codeModel, JDefinedClass genClazz) {

        JMethod toMap = genClazz.method(JMod.PUBLIC | JMod.STATIC, codeModel.directClass(HashMap.class.getCanonicalName()).narrow(String.class).narrow(Object.class), "toMap");
        toMap.param(mHolder.getSourceClazz(), "obj");
        toMap.body().directStatement("java.util.HashMap<String, Object> result = new java.util.HashMap<String, Object>(); result.putAll(((" + genClazz.name() + ")obj).mDoc); result.putAll(((" + genClazz.name() + ")obj).mDocChanges); return result;");

        JMethod toMapList = genClazz.method(JMod.PUBLIC | JMod.STATIC, codeModel.directClass(List.class.getCanonicalName()).narrow(codeModel.directClass(HashMap.class.getCanonicalName()).narrow(String.class).narrow(Object.class)), "toMap");
        toMapList.param(codeModel.directClass(List.class.getCanonicalName()).narrow(mHolder.getSourceClazz()), "obj");
        toMapList.body().directStatement("java.util.List<java.util.HashMap<String, Object>> result = new java.util.ArrayList<java.util.HashMap<String, Object>>(); for(" + mHolder.getSourceClazz().name() + " entry : obj) {result.add(((" + genClazz.name() + ")entry).toMap(entry)); } return result;");
    }


    private void createFromMap(JCodeModel codeModel, JDefinedClass genClazz) {
        JMethod fromMap = genClazz.method(JMod.PUBLIC | JMod.STATIC, mHolder.getSourceClazz(), "fromMap");
        fromMap.param(codeModel.directClass(HashMap.class.getCanonicalName()).narrow(String.class).narrow(Object.class), "obj");
        fromMap.body().directStatement("return new " + genClazz.name() + "(obj);");

        JMethod fromMapList = genClazz.method(JMod.PUBLIC | JMod.STATIC, codeModel.directClass(List.class.getCanonicalName()).narrow(mHolder.getSourceClazz()), "fromMap");
        fromMapList.param(codeModel.directClass(List.class.getCanonicalName()).narrow(codeModel.directClass(HashMap.class.getCanonicalName()).narrow(String.class).narrow(Object.class)), "obj");

        StringBuilder mBuilder = new StringBuilder()
                .append("java.util.List<" + mHolder.getSourceClazz().name() + "> result = new java.util.ArrayList<" + mHolder.getSourceClazz().name() + ">(); \n")
                .append("for(java.util.HashMap<String, Object> entry : obj) { \n")
                .append("result.add(new " + genClazz.name() + "(entry)); \n")
                .append("} \n")
                .append("return result; \n");


        fromMapList.body().directStatement(mBuilder.toString());
    }

    private void createSaveMethod(JCodeModel codeModel, JDefinedClass genClazz, List<CblFieldHolder> attachments) {
        JMethod mSave = genClazz.method(JMod.PUBLIC, codeModel.VOID, "save");
        mSave._throws(CouchbaseLiteException.class);

        StringBuilder builder = new StringBuilder();
        builder.append("com.couchbase.lite.Document doc = kaufland.com.coachbasebinderapi.PersistenceConfig.getInstance().createOrGet(getId()); \n");

        if (attachments.size() <= 0) {
            builder.append("doc.putProperties(mDocChanges); \n");
        } else {
            builder.append("com.couchbase.lite.UnsavedRevision rev = doc.createRevision(); \n");
            builder.append("rev.setProperties(mDocChanges); \n");
            for (CblFieldHolder attachment : attachments) {
                builder.append("rev.setAttachment(\"" + attachment.getDbField() + "\", \"" + attachment.getAttachmentType() + "\", " + attachment.getDbField() + "); \n");
            }

            builder.append("rev.save(); \n");
        }

        mSave.body().directStatement(builder.toString());
    }

    private void createFieldConstant(JDefinedClass genClazz, String fieldName) {
        JFieldVar constant = genClazz.field(JMod.PUBLIC | JMod.STATIC | JMod.FINAL, String.class, fieldName.toUpperCase());
        constant.init(JExpr.lit(fieldName));
    }
}
