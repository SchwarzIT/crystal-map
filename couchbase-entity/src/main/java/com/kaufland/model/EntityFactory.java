package com.kaufland.model;

import com.kaufland.ElementMetaModel;
import com.kaufland.model.entity.BaseEntityHolder;
import com.kaufland.model.entity.WrapperEntityHolder;
import com.kaufland.model.entity.EntityHolder;
import com.kaufland.model.field.CblConstantHolder;
import com.kaufland.model.field.CblDefaultHolder;
import com.kaufland.model.field.CblFieldHolder;
import com.thoughtworks.qdox.model.JavaField;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;

import kaufland.com.coachbasebinderapi.Constant;
import kaufland.com.coachbasebinderapi.Default;
import kaufland.com.coachbasebinderapi.Entity;
import kaufland.com.coachbasebinderapi.Field;

public class EntityFactory {

    public static EntityHolder createEntityHolder(Element cblEntityElement, ElementMetaModel metaModel) {
        return (EntityHolder) create(cblEntityElement, metaModel, new EntityHolder(cblEntityElement.getAnnotation(Entity.class).database()));
    }

    public static WrapperEntityHolder createChildEntityHolder(Element cblEntityElement, ElementMetaModel metaModel) {

        return (WrapperEntityHolder) create(cblEntityElement, metaModel, new WrapperEntityHolder());
    }

    private static BaseEntityHolder create(Element cblEntityElement, ElementMetaModel metaModel, BaseEntityHolder content) {

        content.setSourceElement(cblEntityElement);

        for (Element element : cblEntityElement.getEnclosedElements()) {

            if (element.getKind() == ElementKind.FIELD) {

                JavaField metaField = metaModel.getMetaFor(cblEntityElement).getFieldByName(element.getSimpleName().toString());

                Field cblField = element.getAnnotation(Field.class);
                Constant cblConstant = element.getAnnotation(Constant.class);
                Default cblDefault = element.getAnnotation(Default.class);

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
