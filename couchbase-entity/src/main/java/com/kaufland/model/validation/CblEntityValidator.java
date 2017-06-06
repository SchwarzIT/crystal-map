package com.kaufland.model.validation;

import com.kaufland.Logger;
import com.kaufland.model.source.CblEntityHolder;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.ElementFilter;

import kaufland.com.coachbasebinderapi.CblEntity;
import kaufland.com.coachbasebinderapi.CblField;

public class CblEntityValidator {


    public void validate(CblEntityHolder holder, Logger logger) throws ClassNotFoundException {

        if (holder.getSourceElement().getModifiers().contains(Modifier.PRIVATE)) {
            logger.error(CblEntity.class.getName() + " can not be private", holder.getSourceElement());
        }
        if (holder.getSourceElement().getModifiers().contains(Modifier.FINAL)) {
            logger.error(CblEntity.class.getName() + " can not be final", holder.getSourceElement());
        }


        for (Element member : holder.getSourceElement().getEnclosedElements()) {

            if (member.getKind() == ElementKind.FIELD) {

                CblField fieldAnnotation = member.getAnnotation(CblField.class);
                if (fieldAnnotation != null) {
                    if (!member.getModifiers().contains(Modifier.PRIVATE)) {
                        logger.error(CblField.class.getName() + " must be private", member);
                    }

                    if (!fieldAnnotation.attachmentType().equals("")) {
                        Class<?> classTypeOfField = Class.forName(member.asType().toString());
                        if (!InputStream.class.isAssignableFrom(classTypeOfField) && !URL.class.isAssignableFrom(classTypeOfField)) {
                            logger.error(CblField.class.getName() + " attachments must be Inputstream or URL", member);
                        }
                    }

                }

            } else if (member.getKind() == ElementKind.CONSTRUCTOR) {

                List<ExecutableElement> constructors = ElementFilter.constructorsIn(member.getEnclosedElements());

                if (constructors.size() != 0) {
                    logger.error(CblEntity.class.getName() + " should not have a contructor", member);
                }
            }

        }

    }

}
