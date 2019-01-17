package com.kaufland.model;

import com.kaufland.ElementMetaModel;
import com.kaufland.model.entity.CblBaseEntityHolder;
import com.kaufland.model.entity.CblChildEntityHolder;
import com.kaufland.model.entity.CblEntityHolder;
import com.kaufland.model.field.CblConstantHolder;
import com.kaufland.model.field.CblDefaultHolder;
import com.kaufland.model.field.CblFieldHolder;
import com.thoughtworks.qdox.model.JavaField;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;

import kaufland.com.coachbasebinderapi.CblConstant;
import kaufland.com.coachbasebinderapi.CblDefault;
import kaufland.com.coachbasebinderapi.CblEntity;
import kaufland.com.coachbasebinderapi.CblField;

public class EntityFactory {

    public static CblEntityHolder createEntityHolder(Element cblEntityElement, ElementMetaModel metaModel) {
        return (CblEntityHolder) create(cblEntityElement, metaModel, new CblEntityHolder(cblEntityElement.getAnnotation(CblEntity.class).database()));
    }

    public static CblChildEntityHolder createChildEntityHolder(Element cblEntityElement, ElementMetaModel metaModel) {

        return (CblChildEntityHolder) create(cblEntityElement, metaModel, new CblChildEntityHolder());
    }

    private static CblBaseEntityHolder create(Element cblEntityElement, ElementMetaModel metaModel, CblBaseEntityHolder content) {

        content.setSourceElement(cblEntityElement);

        for (Element element : cblEntityElement.getEnclosedElements()) {

            if (element.getKind() == ElementKind.FIELD) {

                JavaField metaField = metaModel.getMetaFor(cblEntityElement).getFieldByName(element.getSimpleName().toString());

                CblField cblField = element.getAnnotation(CblField.class);
                CblConstant cblConstant = element.getAnnotation(CblConstant.class);
                CblDefault cblDefault = element.getAnnotation(CblDefault.class);

                if (cblField == null && cblConstant == null) {
                    continue;
                }

                if (cblField != null) {

                    CblDefaultHolder defaultHolder = cblDefault != null ? new CblDefaultHolder(cblDefault.value()) : null;
                    CblFieldHolder cblFieldHolder = new CblFieldHolder(cblField, element, metaField, defaultHolder, metaModel);
                    content.getFields().add(cblFieldHolder);
                }

                if (cblConstant != null) {
                    content.getFieldConstants().add(new CblConstantHolder(cblConstant, element, metaField));
                }
            }
        }

        return content;

    }
}
