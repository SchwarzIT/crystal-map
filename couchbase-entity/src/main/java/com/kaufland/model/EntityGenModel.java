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
import com.kaufland.Logger;
import com.kaufland.util.ElementUtil;

import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public EntityGenModel(Element elem) {
        sourceClazzName = elem.getSimpleName().toString();
        sourcePackage = elem.getEnclosingElement().toString();
        mElementClazz = elem;
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

                        if (baseTypeWithGenerics.size() > 1) {
                            for (int i = 1; i < baseTypeWithGenerics.size(); i++) {
                                resturnValue = resturnValue.narrow(Class.forName(baseTypeWithGenerics.get(i)));
                            }
                        }

                        createGetter(genClazz, WordUtils.capitalize(element.getSimpleName().toString()), cblFieldName, resturnValue);

                        JMethod setter = genClazz.method(JMod.PUBLIC, genClazz, "set" + WordUtils.capitalize(element.getSimpleName().toString()));
                        setter.param(resturnValue, "value");

                        if (annotation.attachmentType().equals("")) {
                            setter.body().directStatement("mDocChanges.put(" + cblFieldName.toUpperCase() + ", value); return this;");
                        } else {
                            JFieldVar attachment = genClazz.field(JMod.PRIVATE, resturnValue, cblFieldName);
                            setter.body().directStatement(cblFieldName + "= value; return this;");
                            attachmentFields.add(new String[]{cblFieldName, annotation.attachmentType()});
//                            if (!resturnValue.isAssignableFrom(codeModel.directClass(InputStream.class.getCanonicalName())) && !resturnValue.isAssignableFrom(codeModel.directClass(URL.class.getCanonicalName()))) {
//                                Logger.getInstance().abortWithError("Attachment only supported by FieldType Inputstream or URL", element);
//                            }
                        }

                        createFieldConstant(genClazz, cblFieldName);
                    }
                }
            }

            createSaveMethod(codeModel, genClazz, attachmentFields);


            AbstractJClass annotatedComponent = codeModel.directClass(mElementClazz.asType().toString());
            genClazz._extends(annotatedComponent);


        } catch (JClassAlreadyExistsException e) {
            Logger.getInstance().error("Clazz already exists", mElementClazz);
        } catch (ClassNotFoundException e) {
            Logger.getInstance().error("Clazz not found", mElementClazz);
        }

        return codeModel;


    }

    private void createGetter(JDefinedClass genClazz, String getName, String cblFieldName, AbstractJClass resturnValue) {
        JMethod getter = genClazz.method(JMod.PUBLIC, resturnValue, "get" + getName);
        getter.body().directStatement("return (" + resturnValue.name() + ") mDoc.get(" + cblFieldName.toUpperCase() + ");");
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
