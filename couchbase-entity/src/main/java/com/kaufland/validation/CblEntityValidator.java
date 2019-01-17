package com.kaufland.validation;

import com.kaufland.ElementMetaModel;
import com.kaufland.Logger;
import com.kaufland.util.ElementUtil;
import com.sun.tools.javac.code.Symbol;

import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;

import kaufland.com.coachbasebinderapi.CblChild;
import kaufland.com.coachbasebinderapi.CblConstant;
import kaufland.com.coachbasebinderapi.CblDefault;
import kaufland.com.coachbasebinderapi.CblEntity;
import kaufland.com.coachbasebinderapi.CblField;

public class CblEntityValidator {


    public void validate(Element entityElement, ElementMetaModel elementMetaModel, Logger logger) throws ClassNotFoundException {

        if (entityElement.getModifiers().contains(Modifier.PRIVATE)) {
            logger.error(CblEntity.class.getSimpleName() + " can not be private", entityElement);
        }
        if (entityElement.getModifiers().contains(Modifier.FINAL)) {
            logger.error(CblEntity.class.getSimpleName() + " can not be final", entityElement);
        }


        for (Element member : entityElement.getEnclosedElements()) {

            if (member.getKind() == ElementKind.FIELD) {

                CblField fieldAnnotation = member.getAnnotation(CblField.class);
                CblConstant constantAnnotation = member.getAnnotation(CblConstant.class);
                CblDefault defaultAnnotation = member.getAnnotation(CblDefault.class);
                if (fieldAnnotation != null) {

                    if (constantAnnotation != null) {
                        logger.error("Element can´t be " + CblField.class.getSimpleName() + " and " + CblConstant.class.getSimpleName() + " at the same time", member);
                    }

                    if (!member.getModifiers().contains(Modifier.PRIVATE)) {
                        logger.error(CblField.class.getSimpleName() + " must be private", member);
                    }

                    List<String> clazzes = ElementUtil.splitGenericIfNeeded(member.asType().toString());
                    for (String clazz : clazzes) {
                        if (elementMetaModel.isEntity(clazz)) {
                            logger.error(CblEntity.class.getSimpleName() + " not valid to use as member needs to be " + CblChild.class.getSimpleName(), member);
                        }
                    }


                    if (defaultAnnotation != null) {
                        if (clazzes.size() > 1 || !Arrays.asList(String.class, Long.class, Double.class, Integer.class, Boolean.class).contains(Class.forName(clazzes.get(0)))) {
                            logger.error(CblDefault.class.getSimpleName() + " must be must be String.class, Long.class, Double.class or Integer.class", member);
                        }
                    }

                } else if (constantAnnotation != null) {

                    if (fieldAnnotation != null) {
                        logger.error("Element can´t be " + CblField.class.getSimpleName() + " and " + CblConstant.class.getSimpleName() + " at the same time", member);
                    }

                    if (defaultAnnotation != null) {
                        logger.error(CblDefault.class.getSimpleName() + " can´t be used for " + CblConstant.class.getSimpleName(), member);
                    }

                    if (!member.getModifiers().contains(Modifier.PRIVATE)) {
                        logger.error(CblConstant.class.getSimpleName() + " must be private", member);
                    }
                }

            } else if (member.getKind() == ElementKind.CONSTRUCTOR) {

                Symbol.MethodSymbol constructor = (Symbol.MethodSymbol) member;

                if (constructor.getParameters().size() != 0) {
                    logger.error(CblEntity.class.getSimpleName() + " should not have a contructor", member);
                }
            }

        }

    }
}
