package com.kaufland.validation;

import com.kaufland.ElementMetaModel;
import com.kaufland.Logger;
import com.kaufland.util.ElementUtil;
import com.kaufland.util.FieldExtractionUtil;
import com.sun.tools.javac.code.Symbol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;

import kaufland.com.coachbasebinderapi.Entity;
import kaufland.com.coachbasebinderapi.Field;
import kaufland.com.coachbasebinderapi.Fields;
import kaufland.com.coachbasebinderapi.MapWrapper;

public class PreValidator {


    public void validate(Element entityElement, ElementMetaModel elementMetaModel, Logger logger) throws ClassNotFoundException {

        if (entityElement.getModifiers().contains(Modifier.PRIVATE)) {
            logger.error(Entity.class.getSimpleName() + " can not be private", entityElement);
        }
        if (entityElement.getModifiers().contains(Modifier.FINAL)) {
            logger.error(Entity.class.getSimpleName() + " can not be final", entityElement);
        }

        Fields fields = entityElement.getAnnotation(Fields.class);

        List<String> names = new ArrayList<>();

        for (Field fieldAnnotation : fields.value()) {

            if (fieldAnnotation != null) {

                if(names.contains(fieldAnnotation.name())){
                    logger.warn("duplicated field name", entityElement);
                }


                if (!fieldAnnotation.defaultValue().isEmpty()) {
                    if (fieldAnnotation.list() || !Arrays.asList(String.class.getCanonicalName(), Long.class.getCanonicalName(), Double.class.getCanonicalName(), Integer.class.getCanonicalName(), Boolean.class.getCanonicalName()).contains(FieldExtractionUtil.typeMirror(fieldAnnotation).toString())) {
                        logger.error("defaultValue must be must be String.class, Long.class, Double.class or Integer.class", entityElement);
                    }
                }

                if(fieldAnnotation.readonly() && fieldAnnotation.defaultValue().isEmpty()){
                    logger.warn("defaultValue should not be empty for readonly fields", entityElement);
                }
                names.add(fieldAnnotation.name());

            }

        }

        for (Element member : entityElement.getEnclosedElements()) {

            if (member.getKind() == ElementKind.CONSTRUCTOR) {

                Symbol.MethodSymbol constructor = (Symbol.MethodSymbol) member;

                if (constructor.getParameters().size() != 0) {
                    logger.error(Entity.class.getSimpleName() + " should not have a contructor", member);
                }
            }
        }


    }
}
