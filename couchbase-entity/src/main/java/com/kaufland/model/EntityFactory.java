package com.kaufland.model;

import com.kaufland.ElementMetaModel;
import com.kaufland.model.entity.BaseEntityHolder;
import com.kaufland.model.entity.EntityHolder;
import com.kaufland.model.entity.WrapperEntityHolder;
import com.kaufland.model.field.CblConstantHolder;
import com.kaufland.model.field.CblFieldHolder;

import javax.lang.model.element.Element;

import kaufland.com.coachbasebinderapi.Entity;
import kaufland.com.coachbasebinderapi.Field;
import kaufland.com.coachbasebinderapi.Fields;

public class EntityFactory {

    public static EntityHolder createEntityHolder(Element cblEntityElement, ElementMetaModel metaModel) {
        return (EntityHolder) create(cblEntityElement, metaModel, new EntityHolder(cblEntityElement.getAnnotation(Entity.class).database()));
    }

    public static WrapperEntityHolder createChildEntityHolder(Element cblEntityElement, ElementMetaModel metaModel) {

        return (WrapperEntityHolder) create(cblEntityElement, metaModel, new WrapperEntityHolder());
    }

    private static BaseEntityHolder create(Element cblEntityElement, ElementMetaModel metaModel, BaseEntityHolder content) {

        content.setSourceElement(cblEntityElement);

        Fields fields = cblEntityElement.getAnnotation(Fields.class);


        for (Field cblField : fields.value()) {

            if (cblField == null) {
                continue;
            }

            if (cblField.readonly()) {
                content.getFieldConstants().add(new CblConstantHolder(cblField));
            } else {
                CblFieldHolder cblFieldHolder = new CblFieldHolder(cblField, metaModel);
                content.getFields().add(cblFieldHolder);
            }
        }

        return content;

    }
}
