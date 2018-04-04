package com.kaufland.model.source;

import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.JavaGenericDeclaration;
import com.thoughtworks.qdox.model.JavaType;
import com.thoughtworks.qdox.model.JavaTypeVariable;
import com.thoughtworks.qdox.model.impl.DefaultJavaParameterizedType;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;

import kaufland.com.coachbasebinderapi.CblConstant;
import kaufland.com.coachbasebinderapi.CblDefault;
import kaufland.com.coachbasebinderapi.CblEntity;
import kaufland.com.coachbasebinderapi.CblField;

public class SourceContentParser {

    private Element currentWorkingObject;

    public CblEntityHolder parse(Element cblEntityElement, Map<String, Element> allAnnotatedClazzes, JavaClass clazzWithAnnotation) throws ClassNotFoundException {

        CblEntityHolder content = new CblEntityHolder();

        content.setSourceElement(cblEntityElement);
        CblEntity entityAnnotation = cblEntityElement.getAnnotation(CblEntity.class);
        content.setDbName(entityAnnotation.database());
        content.setId(entityAnnotation.id().equals("") ? null : entityAnnotation.id());

        for (Element element : cblEntityElement.getEnclosedElements()) {

            currentWorkingObject = element;

            if (element.getKind() == ElementKind.FIELD) {

                JavaField metaField = clazzWithAnnotation.getFieldByName(element.getSimpleName().toString());

                CblField cblField = element.getAnnotation(CblField.class);
                CblConstant cblConstant = element.getAnnotation(CblConstant.class);
                CblDefault cblDefault = element.getAnnotation(CblDefault.class);

                if (cblField == null && cblConstant == null) {
                    continue;
                }

                if (cblField != null) {

                    if (StringUtils.isNotBlank(cblField.attachmentType())) {
                        content.getFieldAttachments().add(new CblAttachmentFieldHolder(cblField, element, metaField));
                    } else {
                        CblDefaultHolder defaultHolder = cblDefault != null ? new CblDefaultHolder(cblDefault.value()) : null;
                        CblFieldHolder cblFieldHolder = new CblFieldHolder(cblField, element, metaField, defaultHolder, allAnnotatedClazzes);
                        content.getFields().add(cblFieldHolder);
                    }
                }

                if (cblConstant != null) {
                    content.getFieldConstants().add(new CblConstantHolder(cblConstant, element, metaField));
                }
            }
        }

        return content;

    }

    public Element getCurrentWorkingObject() {
        return currentWorkingObject;
    }
}
