package com.kaufland.model.validation;

import com.kaufland.Logger;
import com.kaufland.model.source.CblEntityHolder;
import com.sun.tools.javac.code.Symbol;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.ElementFilter;

import kaufland.com.coachbasebinderapi.CblConstant;
import kaufland.com.coachbasebinderapi.CblEntity;
import kaufland.com.coachbasebinderapi.CblField;

public class CblEntityValidator {


    public void validate(CblEntityHolder holder, Logger logger) throws ClassNotFoundException {

        if (holder.getSourceElement().getModifiers().contains(Modifier.PRIVATE)) {
            logger.error(CblEntity.class.getSimpleName() + " can not be private", holder.getSourceElement());
        }
        if (holder.getSourceElement().getModifiers().contains(Modifier.FINAL)) {
            logger.error(CblEntity.class.getSimpleName() + " can not be final", holder.getSourceElement());
        }


        for (Element member : holder.getSourceElement().getEnclosedElements()) {

            if (member.getKind() == ElementKind.FIELD) {

                CblField fieldAnnotation = member.getAnnotation(CblField.class);
                CblConstant constantAnnotation = member.getAnnotation(CblConstant.class);
                if (fieldAnnotation != null) {

                    if(constantAnnotation != null){
                        logger.error("Element can´t be "+ CblField.class.getName() + " and "+ CblConstant.class.getName() + " at the same time", member);
                    }

                    if (!member.getModifiers().contains(Modifier.PRIVATE)) {
                        logger.error(CblField.class.getSimpleName() + " must be private", member);
                    }

                    if (!fieldAnnotation.attachmentType().equals("")) {
                        Class<?> classTypeOfField = Class.forName(member.asType().toString());
                        if (!InputStream.class.isAssignableFrom(classTypeOfField) && !URL.class.isAssignableFrom(classTypeOfField)) {
                            logger.error(CblField.class.getSimpleName() + " attachments must be Inputstream or URL", member);
                        }
                    }
                }else if(constantAnnotation != null){

                    if(fieldAnnotation != null){
                        logger.error("Element can´t be "+ CblField.class.getName() + " and "+ CblConstant.class.getName() + " at the same time", member);
                    }

                    if (!member.getModifiers().contains(Modifier.PRIVATE)) {
                        logger.error(CblConstant.class.getSimpleName() + " must be private", member);
                    }
                }

            } else if (member.getKind() == ElementKind.CONSTRUCTOR) {

                Symbol.MethodSymbol constructor = (Symbol.MethodSymbol)member;

                if (constructor.getParameters().size() != 0) {
                    logger.error(CblEntity.class.getSimpleName() + " should not have a contructor", member);
                }
            }

        }

    }

}
