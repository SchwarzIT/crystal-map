package com.kaufland.model;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.couchbase.lite.UnsavedRevision;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.JClassAlreadyExistsException;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JDirectClass;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JFieldVar;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JPackage;
import com.kaufland.Logger;
import com.kaufland.util.ElementUtil;

import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;

import kaufland.com.coachbasebinderapi.CblField;


/**
 * Created by sbra0902 on 24.05.17.
 */

public class EntityGenModel implements GenerationModel {

    private String sourceClazzName;

    private String sourcePackage;

    private Element mElementClazz;

    private Map<String, ? extends Element> mAnnotatedElements;

    public EntityGenModel(Element elem, Map<String, ? extends Element> annotatedElements) {
        sourceClazzName = elem.getSimpleName().toString();
        sourcePackage = elem.getEnclosingElement().toString();
        mElementClazz = elem;
        mAnnotatedElements = annotatedElements;
    }

    public JCodeModel generateModel() {

        JCodeModel codeModel = new JCodeModel();

        codeModel.ref(UnsavedRevision.class);
        codeModel.ref(Document.class);

        JPackage mJPackage = codeModel._package(sourcePackage);

        try {
            JDefinedClass genClazz = mJPackage._class(sourceClazzName + "Entity");

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

            createGetter(genClazz, "Id", "_ID", codeModel.directClass(String.class.getCanonicalName()));

            createFieldConstant(genClazz, "_id");


            List<String[]> attachmentFields = new ArrayList<>();
            for (Element element : mElementClazz.getEnclosedElements()) {
                if (element.getKind() == ElementKind.FIELD) {
                    CblField annotation = element.getAnnotation(CblField.class);

                    if (annotation != null) {

                        String cblFieldName = annotation.value();
                        if (cblFieldName.equals("")) {
                            cblFieldName = element.getSimpleName().toString();
                        }

                        List<String> baseTypeWithGenerics = ElementUtil.splitGenericIfNeeded(element.asType().toString());

                        //GetMethod
                        AbstractJClass resturnValue = codeModel.directClass(baseTypeWithGenerics.get(0));

                        AbstractJClass nestedSubEntity = mAnnotatedElements.containsKey(resturnValue.name()) ? resturnValue : null;
                        boolean isEntityGenericParam = false;

                        if (baseTypeWithGenerics.size() > 1) {
                            for (int i = 1; i < baseTypeWithGenerics.size(); i++) {
                                JDirectClass clazz = codeModel.directClass(baseTypeWithGenerics.get(i));
                                resturnValue = resturnValue.narrow(clazz);
                                if (mAnnotatedElements.containsKey(clazz.name())) {
                                    nestedSubEntity = codeModel.directClass(baseTypeWithGenerics.get(i));
                                    isEntityGenericParam = true;
                                }
                            }
                        }


                        JMethod setter = genClazz.method(JMod.PUBLIC, genClazz, "set" + WordUtils.capitalize(element.getSimpleName().toString()));
                        setter.param(resturnValue, "value");

                        if (nestedSubEntity != null) {
                            String toMapParam = isEntityGenericParam ? "java.util.List<" + nestedSubEntity.name()+">" : nestedSubEntity.name();
                            setter.body().directStatement("mDocChanges.put(" + cblFieldName.toUpperCase() + ", " + nestedSubEntity.name() + "Entity.toMap((" + toMapParam + ")value)); return this;");
                            JMethod getter = genClazz.method(JMod.PUBLIC, resturnValue, "get" + WordUtils.capitalize(element.getSimpleName().toString()));
                            String fromMapParam = isEntityGenericParam ? "java.util.List<java.util.HashMap<String, Object>>" : "java.util.HashMap<String, Object>";
                            getter.body().directStatement("return (" + resturnValue.fullName() + ") " + nestedSubEntity.name() + "Entity.fromMap((" + fromMapParam + ")mDoc.get(" + cblFieldName.toUpperCase() + "));");

                        } else if (annotation.attachmentType().equals("")) {
                            setter.body().directStatement("mDocChanges.put(" + cblFieldName.toUpperCase() + ", value); return this;");
                            createGetter(genClazz, WordUtils.capitalize(element.getSimpleName().toString()), cblFieldName, resturnValue);
                        } else {
                            JFieldVar attachment = genClazz.field(JMod.PRIVATE, resturnValue, cblFieldName);
                            setter.body().directStatement(cblFieldName + "= value; return this;");
                            attachmentFields.add(new String[]{cblFieldName, annotation.attachmentType()});
                            createGetter(genClazz, WordUtils.capitalize(element.getSimpleName().toString()), cblFieldName, resturnValue);
                        }

                        createFieldConstant(genClazz, cblFieldName);
                    }
                }
            }

            createFromMap(codeModel, genClazz, codeModel.directClass(sourceClazzName));
            createToMap(codeModel, genClazz, codeModel.directClass(sourceClazzName));

            createSaveMethod(codeModel, genClazz, attachmentFields);

            createDeleteMethod(codeModel, genClazz);


            AbstractJClass annotatedComponent = codeModel.directClass(mElementClazz.asType().toString());
            genClazz._extends(annotatedComponent);


        } catch (JClassAlreadyExistsException e) {
            Logger.getInstance().error("Clazz already exists", mElementClazz);
        }

        return codeModel;


    }

    private void createDeleteMethod(JCodeModel codeModel, JDefinedClass genClazz) {
        JMethod delete = genClazz.method(JMod.PUBLIC, codeModel.VOID, "delete");
        delete._throws(CouchbaseLiteException.class);
        delete.body().directStatement("kaufland.com.coachbasebinderapi.PersistenceConfig.getInstance().createOrGet(getId()).delete();");
    }

    private void createGetter(JDefinedClass genClazz, String getName, String cblFieldName, AbstractJClass resturnValue) {
        JMethod getter = genClazz.method(JMod.PUBLIC, resturnValue, "get" + getName);
        getter.body().directStatement("return (" + resturnValue.name() + ") mDoc.get(" + cblFieldName.toUpperCase() + ");");
    }

    private void createToMap(JCodeModel codeModel, JDefinedClass genClazz, JDirectClass sourceClazz) {

        JMethod toMap = genClazz.method(JMod.PUBLIC | JMod.STATIC, codeModel.directClass(HashMap.class.getCanonicalName()).narrow(String.class).narrow(Object.class), "toMap");
        toMap.param(sourceClazz, "obj");
        toMap.body().directStatement("java.util.HashMap<String, Object> result = new java.util.HashMap<String, Object>(); result.putAll((("+ genClazz.name() +")obj).mDoc); result.putAll((("+ genClazz.name() +")obj).mDocChanges); return result;");

        JMethod toMapList = genClazz.method(JMod.PUBLIC | JMod.STATIC, codeModel.directClass(List.class.getCanonicalName()).narrow(codeModel.directClass(HashMap.class.getCanonicalName()).narrow(String.class).narrow(Object.class)), "toMap");
        toMapList.param(codeModel.directClass(List.class.getCanonicalName()).narrow(sourceClazz), "obj");
        toMapList.body().directStatement("java.util.List<java.util.HashMap<String, Object>> result = new java.util.ArrayList<java.util.HashMap<String, Object>>(); for(" + sourceClazz.name() + " entry : obj) {result.add((("+ genClazz.name() +")entry).toMap(entry)); } return result;");
    }


    private void createFromMap(JCodeModel codeModel, JDefinedClass genClazz, JDirectClass sourceClazz) {
        JMethod fromMap = genClazz.method(JMod.PUBLIC | JMod.STATIC, sourceClazz, "fromMap");
        fromMap.param(codeModel.directClass(HashMap.class.getCanonicalName()).narrow(String.class).narrow(Object.class), "obj");
        fromMap.body().directStatement("return new " + genClazz.name() + "(obj);");

        JMethod fromMapList = genClazz.method(JMod.PUBLIC | JMod.STATIC, codeModel.directClass(List.class.getCanonicalName()).narrow(sourceClazz), "fromMap");
        fromMapList.param(codeModel.directClass(List.class.getCanonicalName()).narrow(codeModel.directClass(HashMap.class.getCanonicalName()).narrow(String.class).narrow(Object.class)), "obj");

        StringBuilder mBuilder = new StringBuilder()
                .append("java.util.List<" + sourceClazz.name() + "> result = new java.util.ArrayList<" + sourceClazz.name() + ">(); \n")
                .append("for(java.util.HashMap<String, Object> entry : obj) { \n")
                .append("result.add(new " + genClazz.name() + "(entry)); \n")
                .append("} \n")
                .append("return result; \n");


        fromMapList.body().directStatement(mBuilder.toString());
    }

    private void createSaveMethod(JCodeModel codeModel, JDefinedClass genClazz, List<String[]> attachments) {
        JMethod mSave = genClazz.method(JMod.PUBLIC, codeModel.VOID, "save");
        mSave._throws(CouchbaseLiteException.class);

        StringBuilder builder = new StringBuilder();
        builder.append("com.couchbase.lite.Document doc = kaufland.com.coachbasebinderapi.PersistenceConfig.getInstance().createOrGet(getId()); \n");

        if (attachments.size() <= 0) {
            builder.append("doc.putProperties(mDocChanges); \n");
        } else {
            builder.append("com.couchbase.lite.UnsavedRevision rev = doc.createRevision(); \n");
            builder.append("rev.setProperties(mDocChanges); \n");
            for (String[] attachment : attachments) {
                builder.append("rev.setAttachment(\"" + attachment[0] + "\", \"" + attachment[1] + "\", " + attachment[0] + "); \n");
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
