package com.kaufland.util;

import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;

import kaufland.com.coachbasebinderapi.Field;

public class FieldExtractionUtil {

    public static TypeMirror typeMirror(Field annotation) {
        try {
            annotation.type();
        } catch (MirroredTypeException mte) {
            return mte.getTypeMirror();
        }
        return null;
    }
}
