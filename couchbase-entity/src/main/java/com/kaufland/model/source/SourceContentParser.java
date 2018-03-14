package com.kaufland.model.source;

import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JDirectClass;
import com.kaufland.util.ElementUtil;

import java.util.List;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;

import kaufland.com.coachbasebinderapi.CblConstant;
import kaufland.com.coachbasebinderapi.CblEntity;
import kaufland.com.coachbasebinderapi.CblField;

public class SourceContentParser {


    public CblEntityHolder parse(Element cblEntityElement, Map<String, Element> allAnnotatedFields, JCodeModel model) throws ClassNotFoundException {

        CblEntityHolder content = new CblEntityHolder();

        content.setSourceClazz(model.directClass(cblEntityElement.toString()));
        content.setSourceElement(cblEntityElement);
        content.setDbName(cblEntityElement.getAnnotation(CblEntity.class).database());

        for (Element element : cblEntityElement.getEnclosedElements()) {

            if (element.getKind() == ElementKind.FIELD) {

                CblBaseFieldHolder fieldHolder = null;

                List<String> baseTypeWithGenerics = ElementUtil.splitGenericIfNeeded(element.asType().toString());

                AbstractJClass fieldType = model.directClass(baseTypeWithGenerics.get(0));

                CblField annotation = element.getAnnotation(CblField.class);
                CblConstant constant = element.getAnnotation(CblConstant.class);

                if(annotation == null && constant == null){
                    continue;
                }

                if (annotation != null) {
                    CblFieldHolder cblFieldHolder = new CblFieldHolder();

                    cblFieldHolder.setAttachmentType(annotation.attachmentType());
                    cblFieldHolder.setDbField(annotation.value().equals("") ? element.getSimpleName().toString() : annotation.value());

                    if (allAnnotatedFields.containsKey(fieldType.name())) {
                        cblFieldHolder.setSubEntityName(fieldType.name()+ "Entity");
                        fieldType = model.directClass(fieldType.fullName()+ "Entity");
                    }

                    if (baseTypeWithGenerics.size() > 1) {
                        for (int i = 1; i < baseTypeWithGenerics.size(); i++) {


                            JDirectClass mClazz = model.directClass(baseTypeWithGenerics.get(i));

                            if (allAnnotatedFields.containsKey(mClazz.name())) {
                                cblFieldHolder.setSubEntityIsTypeParam(true);
                                cblFieldHolder.setSubEntityName(mClazz.name() + "Entity");
                                fieldType = fieldType.narrow(model.directClass(mClazz.fullName()+ "Entity"));
                            }else{
                                fieldType = fieldType.narrow(mClazz);
                            }
                        }
                    }
                    fieldHolder = cblFieldHolder;
                }

                if(constant != null){
                    CblConstantHolder holder = new CblConstantHolder();
                    holder.setConstantValue(constant.constant());
                    holder.setDbField(constant.value().equals("") ? element.getSimpleName().toString() : constant.value());
                    fieldHolder = holder;
                }

                fieldHolder.setType(fieldType);
                fieldHolder.setClazzFieldName(element.toString());
                fieldHolder.setFieldElement(element);

                content.getFields().add(fieldHolder);
            }
        }

        //Add Id Field
        CblFieldHolder idHolder = new CblFieldHolder();
        idHolder.setClazzFieldName("id");
        idHolder.setDbField("_id");
        idHolder.setType(model.directClass(String.class.getCanonicalName()));

        content.getFields().add(idHolder);

        return content;

    }
}
