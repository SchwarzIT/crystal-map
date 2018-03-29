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

public class TypeUtil {

    public static ParameterizedTypeName createHashMapStringObject(){
        return ParameterizedTypeName.get(HashMap.class, String.class, Object.class);
    }

    public static ParameterizedTypeName createMapStringObject(){
        return ParameterizedTypeName.get(Map.class, String.class, Object.class);
    }

    public static ParameterizedTypeName createListWithMapStringObject(){
        return ParameterizedTypeName.get(ClassName.get(List.class), ParameterizedTypeName.get(Map.class, String.class, Object.class));
    }
    public static ParameterizedTypeName createArrayListWithMapStringObject(){
        return ParameterizedTypeName.get(ClassName.get(ArrayList.class), ParameterizedTypeName.get(Map.class, String.class, Object.class));
    }

    private static TypeName[] convertJavaTypeToTypeName(List<JavaType> type, String subEntity){

        List<TypeName> types = new ArrayList<>();

        for (JavaType javaType : type) {

            types.add(parseMetaType((JavaClass) javaType, subEntity));
        }

        return types.toArray(new TypeName[types.size()]);
    }

    public static TypeName parseMetaType(JavaClass type, String subEntity){

        String simpleName = subEntity != null && subEntity.contains(type.getSimpleName()) ? subEntity : type.getSimpleName();
        ClassName baseType = ClassName.get(type.getPackageName(), simpleName);

        if(type instanceof DefaultJavaParameterizedType){

            List<JavaType> typeArguments = ((DefaultJavaParameterizedType) type).getActualTypeArguments();

            if(typeArguments.size() > 0) {
                return ParameterizedTypeName.get(baseType, convertJavaTypeToTypeName(typeArguments, subEntity));
            }
        }

        return baseType;
    }

    public static TypeName parseMetaType(JavaClass type) {
        return parseMetaType(type, null);
    }
}
