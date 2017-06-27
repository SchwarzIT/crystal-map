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
import com.kaufland.model.source.CblBaseFieldHolder;
import com.kaufland.model.source.CblConstantHolder;
import com.kaufland.model.source.CblEntityHolder;
import com.kaufland.model.source.CblFieldHolder;
import com.kaufland.util.ConversionUtil;

import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        ctr.body().directStatement("rebind(doc);");

        JMethod rebindMethod = genClazz.method(JMod.PUBLIC, codeModel.VOID, "rebind");
        rebindMethod.param(mapClazz, "doc");
        rebindMethod.body().directStatement("mDoc = doc != null ? doc : new java.util.HashMap<String, Object>(); mDocChanges = new java.util.HashMap<String, Object>();");


        List<CblFieldHolder> attachmentFields = new ArrayList<>();

        List<CblConstantHolder> constantFields = new ArrayList<>();

        for (CblBaseFieldHolder baseFieldHolder : mHolder.getFields()) {
            JMethod getter = genClazz.method(JMod.PUBLIC, baseFieldHolder.getType(), "get" + WordUtils.capitalize(baseFieldHolder.getClazzFieldName()));

            createFieldConstant(genClazz, baseFieldHolder.getDbField(), ConversionUtil.convertCamelToUnderscore(baseFieldHolder.getDbField()).toUpperCase());

            if (baseFieldHolder instanceof CblConstantHolder) {
                createGetterBodyDefault(baseFieldHolder.getType(), baseFieldHolder.getDbField(), getter);
                createFieldConstant(genClazz, ((CblConstantHolder) baseFieldHolder).getConstantValue(), "DOC_" + ConversionUtil.convertCamelToUnderscore(baseFieldHolder.getDbField()).toUpperCase());
                constantFields.add((CblConstantHolder) baseFieldHolder);
                continue;
            }

            CblFieldHolder fieldHolder = (CblFieldHolder) baseFieldHolder;
            JMethod setter = genClazz.method(JMod.PUBLIC, genClazz, "set" + WordUtils.capitalize(fieldHolder.getClazzFieldName().toString()));
            setter.param(fieldHolder.getType(), "value");


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
                createGetterBodyAttachment(fieldHolder.getType(), fieldHolder.getDbField(), getter);
            }
        }

        createFromMap(codeModel, genClazz);
        createToMap(codeModel, genClazz);

        createSaveMethod(codeModel, genClazz, attachmentFields, constantFields);

        createDeleteMethod(codeModel, genClazz);

        genClazz._extends(mHolder.getSourceClazz());

        return codeModel;


    }

    private void createGetterBodyAttachment(AbstractJClass resturnValue, String dbField, JMethod getter) {


        getter._throws(CouchbaseLiteException.class);
        StringBuilder builder = new StringBuilder();

        builder.append("com.couchbase.lite.Document doc = kaufland.com.coachbasebinderapi.PersistenceConfig.getInstance().createOrGet(getId()); \n");

        builder.append("if(doc.getCurrentRevision() != null && doc.getCurrentRevision().getAttachments() != null &&  doc.getCurrentRevision().getAttachments().size() > 0) {\n");
        builder.append("return doc.getCurrentRevision().getAttachments().get(0).getContent(); \n");
        builder.append("} \n");
        builder.append("return null; \n");

        getter.body().directStatement(builder.toString());

    }

    private void createSetterBodyDefault(CblFieldHolder fieldHolder, JMethod setter) {
        setter.body().directStatement("mDocChanges.put(" + ConversionUtil.convertCamelToUnderscore(fieldHolder.getDbField()).toUpperCase() + ", value); return this;");
    }

    private void createGetterBodySubEntity(CblFieldHolder fieldHolder, JMethod getter) {
        StringBuilder builder = new StringBuilder();
        builder.append("return (" + fieldHolder.getType().fullName() + ") " + fieldHolder.getSubEntityName() + ".fromMap((");
        if (fieldHolder.isSubEntityIsTypeParam()) {
            builder.append("java.util.List<java.util.HashMap<String, Object>>");
        } else {
            builder.append("java.util.HashMap<String, Object>");
        }
        builder.append(")mDoc.get(" + ConversionUtil.convertCamelToUnderscore(fieldHolder.getDbField()).toUpperCase() + "));");

        getter.body().directStatement(builder.toString());
    }

    private void createSetterBodySubEntity(CblFieldHolder fieldHolder, JMethod setter) {
        StringBuilder builder = new StringBuilder();
        builder.append("mDocChanges.put(" + ConversionUtil.convertCamelToUnderscore(fieldHolder.getDbField()).toUpperCase() + ", " + fieldHolder.getSubEntityName() + ".toMap((");

        if (fieldHolder.isSubEntityIsTypeParam()) {
            builder.append(fieldHolder.getType().fullName());
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
        getter.body().directStatement("return (" + resturnValue.fullName() + ") mDoc.get(" + ConversionUtil.convertCamelToUnderscore(dbField).toUpperCase() + ");");
    }

    private void createToMap(JCodeModel codeModel, JDefinedClass genClazz) {

        JMethod toMap = genClazz.method(JMod.PUBLIC | JMod.STATIC, codeModel.directClass(HashMap.class.getCanonicalName()).narrow(String.class).narrow(Object.class), "toMap");
        toMap.param(genClazz, "obj");

        StringBuilder builderSingle = new StringBuilder();
        builderSingle.append("if(obj == null){ \n");
        builderSingle.append("return null; \n");
        builderSingle.append("} \n");
        builderSingle.append("java.util.HashMap<String, Object> result = new java.util.HashMap<String, Object>(); \n");
        builderSingle.append("result.putAll(obj.mDoc); \n");
        builderSingle.append("result.putAll(obj.mDocChanges);\n");
        builderSingle.append("return result;\n");
        toMap.body().directStatement(builderSingle.toString());

        JMethod toMapList = genClazz.method(JMod.PUBLIC | JMod.STATIC, codeModel.directClass(List.class.getCanonicalName()).narrow(codeModel.directClass(HashMap.class.getCanonicalName()).narrow(String.class).narrow(Object.class)), "toMap");
        toMapList.param(codeModel.directClass(List.class.getCanonicalName()).narrow(genClazz), "obj");

        StringBuilder builderMulti = new StringBuilder();
        builderMulti.append("if(obj == null) return null; \n");
        builderMulti.append("java.util.List<java.util.HashMap<String, Object>> result = new java.util.ArrayList<java.util.HashMap<String, Object>>(); \n");
        builderMulti.append("for(" + genClazz.name() + " entry : obj) {\n");
        builderMulti.append("result.add(((" + genClazz.name() + ")entry).toMap(entry));\n");
        builderMulti.append("}\n");
        builderMulti.append("return result;\n");
        toMapList.body().directStatement(builderMulti.toString());
    }


    private void createFromMap(JCodeModel codeModel, JDefinedClass genClazz) {
        JMethod fromMap = genClazz.method(JMod.PUBLIC | JMod.STATIC, genClazz, "fromMap");
        fromMap.param(codeModel.directClass(HashMap.class.getCanonicalName()).narrow(String.class).narrow(Object.class), "obj");
        fromMap.body().directStatement("return obj != null ? new " + genClazz.name() + "(obj) : null;");

        JMethod fromMapList = genClazz.method(JMod.PUBLIC | JMod.STATIC, codeModel.directClass(List.class.getCanonicalName()).narrow(genClazz), "fromMap");
        fromMapList.param(codeModel.directClass(List.class.getCanonicalName()).narrow(codeModel.directClass(HashMap.class.getCanonicalName()).narrow(String.class).narrow(Object.class)), "obj");

        StringBuilder mBuilder = new StringBuilder()
                .append("if(obj != null) { \n")
                .append("java.util.List<" + genClazz.name() + "> result = new java.util.ArrayList<" + genClazz.name() + ">(); \n")
                .append("for(java.util.HashMap<String, Object> entry : obj) { \n")
                .append("result.add(new " + genClazz.name() + "(entry)); \n")
                .append("} \n")
                .append("return result; \n")
                .append("} \n")
                .append("return null; \n");


        fromMapList.body().directStatement(mBuilder.toString());
    }

    private void createSaveMethod(JCodeModel codeModel, JDefinedClass genClazz, List<CblFieldHolder> attachments, List<CblConstantHolder> constantFields) {
        JMethod mSave = genClazz.method(JMod.PUBLIC, codeModel.VOID, "save");
        mSave._throws(CouchbaseLiteException.class);

        StringBuilder builder = new StringBuilder();
        builder.append("com.couchbase.lite.Document doc = kaufland.com.coachbasebinderapi.PersistenceConfig.getInstance().createOrGet(getId()); \n");

        for (CblConstantHolder constant : constantFields) {
            builder.append("mDocChanges.put(\"" + constant.getDbField() + "\",\"" + constant.getConstantValue() + "\"); \n");
        }

        builder.append("java.util.HashMap<String, Object> temp = new java.util.HashMap<String, Object>(); \n");
        builder.append("if(doc.getProperties() != null){ \n");
        builder.append("temp.putAll(doc.getProperties()); \n");
        builder.append("} \n");
        builder.append("if(mDocChanges != null){ \n");
        builder.append("temp.putAll(mDocChanges); \n");
        builder.append("} \n");
        builder.append("doc.putProperties(temp); \n");

        if (attachments.size() > 0) {

            builder.append("com.couchbase.lite.UnsavedRevision rev = doc.createRevision(); \n");
            for (CblFieldHolder attachment : attachments) {

                builder.append("if(" + attachment.getDbField() + " != null){ \n");
                builder.append("rev.setAttachment(\"" + attachment.getDbField() + "\", \"" + attachment.getAttachmentType() + "\", " + attachment.getDbField() + "); \n");
                builder.append("} \n");
            }
            builder.append("rev.save(); \n");
        }
        builder.append("rebind(doc.getProperties()); \n");

        mSave.body().directStatement(builder.toString());
    }

    private void createFieldConstant(JDefinedClass genClazz, String fieldName, String constName) {
        JFieldVar constant = genClazz.field(JMod.PUBLIC | JMod.STATIC | JMod.FINAL, String.class, constName);
        constant.init(JExpr.lit(fieldName));
    }
}
