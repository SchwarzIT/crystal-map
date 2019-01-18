package com.kaufland.validation;

import com.kaufland.ElementMetaModel;
import com.kaufland.Logger;
import com.kaufland.util.ElementUtil;
import com.sun.tools.javac.code.Symbol;

import java.util.Arrays;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;

import kaufland.com.coachbasebinderapi.MapWrapper;
import kaufland.com.coachbasebinderapi.Constant;
import kaufland.com.coachbasebinderapi.Default;
import kaufland.com.coachbasebinderapi.Entity;
import kaufland.com.coachbasebinderapi.Field;

public class PreValidator {


    public void validate(Element entityElement, ElementMetaModel elementMetaModel, Logger logger) throws ClassNotFoundException {

        if (entityElement.getModifiers().contains(Modifier.PRIVATE)) {
            logger.error(Entity.class.getSimpleName() + " can not be private", entityElement);
        }
        if (entityElement.getModifiers().contains(Modifier.FINAL)) {
            logger.error(Entity.class.getSimpleName() + " can not be final", entityElement);
        }


        for (Element member : entityElement.getEnclosedElements()) {

            if (member.getKind() == ElementKind.FIELD) {

                Field fieldAnnotation = member.getAnnotation(Field.class);
                Constant constantAnnotation = member.getAnnotation(Constant.class);
                Default defaultAnnotation = member.getAnnotation(Default.class);
                if (fieldAnnotation != null) {

                    if (constantAnnotation != null) {
                        logger.error("Element can´t be " + Field.class.getSimpleName() + " and " + Constant.class.getSimpleName() + " at the same time", member);
                    }

                    if (!member.getModifiers().contains(Modifier.PRIVATE)) {
                        logger.error(Field.class.getSimpleName() + " must be private", member);
                    }

                    List<String> clazzes = ElementUtil.splitGenericIfNeeded(member.asType().toString());
                    for (String clazz : clazzes) {
                        if (elementMetaModel.isEntity(clazz)) {
                            logger.error(Entity.class.getSimpleName() + " not valid to use as member needs to be " + MapWrapper.class.getSimpleName(), member);
                        }
                    }


                    if (defaultAnnotation != null) {
                        if (clazzes.size() > 1 || !Arrays.asList(String.class, Long.class, Double.class, Integer.class, Boolean.class).contains(Class.forName(clazzes.get(0)))) {
                            logger.error(Default.class.getSimpleName() + " must be must be String.class, Long.class, Double.class or Integer.class", member);
                        }
                    }

                } else if (constantAnnotation != null) {

                    if (fieldAnnotation != null) {
                        logger.error("Element can´t be " + Field.class.getSimpleName() + " and " + Constant.class.getSimpleName() + " at the same time", member);
                    }

                    if (defaultAnnotation != null) {
                        logger.error(Default.class.getSimpleName() + " can´t be used for " + Constant.class.getSimpleName(), member);
                    }

                    if (!member.getModifiers().contains(Modifier.PRIVATE)) {
                        logger.error(Constant.class.getSimpleName() + " must be private", member);
                    }
                }

            } else if (member.getKind() == ElementKind.CONSTRUCTOR) {

                Symbol.MethodSymbol constructor = (Symbol.MethodSymbol) member;

                if (constructor.getParameters().size() != 0) {
                    logger.error(Entity.class.getSimpleName() + " should not have a contructor", member);
                }
            }

        }

    }
}
