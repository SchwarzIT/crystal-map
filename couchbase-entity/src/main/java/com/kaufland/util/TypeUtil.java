package com.kaufland.util;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaType;
import com.thoughtworks.qdox.model.impl.DefaultJavaParameterizedType;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.type.TypeMirror;

public class TypeUtil {

    public static ParameterizedTypeName createHashMapStringObject() {
        return ParameterizedTypeName.get(HashMap.class, String.class, Object.class);
    }

    public static ParameterizedTypeName createMapStringObject() {
        return ParameterizedTypeName.get(Map.class, String.class, Object.class);
    }

    public static ParameterizedTypeName createListWithMapStringObject() {
        return ParameterizedTypeName.get(ClassName.get(List.class), ParameterizedTypeName.get(Map.class, String.class, Object.class));
    }

    public static ParameterizedTypeName createArrayListWithMapStringObject() {
        return ParameterizedTypeName.get(ClassName.get(ArrayList.class), ParameterizedTypeName.get(Map.class, String.class, Object.class));
    }

    public static String getSimpleName(TypeMirror type) {
        String[] parts = type.toString().split("\\.");
        return parts.length > 1 ? parts[parts.length - 1] : parts[0];
    }

    public static String getPackage(TypeMirror type) {
        int lastIndexOf = type.toString().lastIndexOf(".");
        return lastIndexOf >= 0 ? type.toString().substring(0, lastIndexOf) : type.toString();
    }

    public static TypeName parseMetaType(TypeMirror type, boolean list, String subEntity) {

        String simpleName = subEntity != null && subEntity.contains(getSimpleName(type)) ? subEntity : getSimpleName(type);

        TypeName baseType = null;
        try{
            baseType = ClassName.get(getPackage(type), simpleName);
        }catch (IllegalArgumentException e){
            baseType = TypeName.get(type);
        }

        if (list) {
            return ParameterizedTypeName.get(ClassName.get(List.class), baseType);
        }
        return baseType;
    }
}
