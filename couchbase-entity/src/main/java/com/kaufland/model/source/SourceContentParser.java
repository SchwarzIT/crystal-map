package com.kaufland.model.source;

import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JDirectClass;
import com.kaufland.util.ElementUtil;

import java.util.List;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;

import kaufland.com.coachbasebinderapi.CblField;

/**
 * Created by sbra0902 on 02.06.17.
 */

public class SourceContentParser {


    public CblEntityHolder parse(Element cblEntityElement, Map<String, Element> allAnnotatedFields, JCodeModel model) throws ClassNotFoundException {

        CblEntityHolder content = new CblEntityHolder();

        content.setSourceClazz(model.directClass(cblEntityElement.toString()));
        content.setSourceElement(cblEntityElement);

        for (Element element : cblEntityElement.getEnclosedElements()) {

            if (element.getKind() == ElementKind.FIELD) {
                CblField annotation = element.getAnnotation(CblField.class);

                if (annotation != null) {

                    CblFieldHolder fieldHolder = new CblFieldHolder();

                    fieldHolder.setFieldElement(element);
                    fieldHolder.setDbField(annotation.value().equals("") ? element.getSimpleName().toString() : annotation.value());
                    fieldHolder.setAttachmentType(annotation.attachmentType());
                    fieldHolder.setClazzFieldName(element.toString());

                    List<String> baseTypeWithGenerics = ElementUtil.splitGenericIfNeeded(element.asType().toString());

                    AbstractJClass fieldType = model.directClass(baseTypeWithGenerics.get(0));

                    if (allAnnotatedFields.containsKey(fieldType.name())) {
                        fieldHolder.setSubEntityName(fieldType.name());
                    }

                    if (baseTypeWithGenerics.size() > 1) {
                        for (int i = 1; i < baseTypeWithGenerics.size(); i++) {

                            JDirectClass mClazz = model.directClass(baseTypeWithGenerics.get(i));
                            fieldType = fieldType.narrow(mClazz);
                            if (allAnnotatedFields.containsKey(mClazz.name())) {
                                fieldHolder.setSubEntityIsTypeParam(true);
                                fieldHolder.setSubEntityName(mClazz.name());
                            }
                        }
                    }

                    fieldHolder.setType(fieldType);

                    content.getFields().add(fieldHolder);


                }
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
