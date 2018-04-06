package com.kaufland.model.field;

import com.kaufland.ElementMetaModel;
import com.kaufland.util.TypeUtil;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.JavaType;
import com.thoughtworks.qdox.model.impl.DefaultJavaParameterizedType;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

import kaufland.com.coachbasebinderapi.CblField;

public class CblFieldHolder extends CblBaseFieldHolder {

    private String subEntityPackage;

    private String subEntitySimpleName;

    private boolean subEntityIsTypeParam;

    private CblDefaultHolder defaultHolder;

    public CblFieldHolder(CblField field, Element fieldElement, JavaField metaField, CblDefaultHolder defaultHolder, ElementMetaModel metaModel) {
        super(field.value(), fieldElement, metaField);
        this.defaultHolder = defaultHolder;


        String typeName = metaField.getType().getCanonicalName();
        if (metaModel.isChildEntity(typeName)) {
            subEntitySimpleName = metaField.getType().getSimpleName() + "Entity";
            subEntityPackage = metaField.getType().getPackageName();
        } else if (metaField.getType() instanceof DefaultJavaParameterizedType) {
            for (JavaType typeParameter : ((DefaultJavaParameterizedType) metaField.getType()).getActualTypeArguments()) {

                String canonicalName = typeParameter.getCanonicalName();
                if (metaModel.isChildEntity(canonicalName)) {
                    JavaClass metaClazz = metaModel.getMetaFor(canonicalName);
                    subEntitySimpleName = metaClazz.getSimpleName() + "Entity";
                    subEntityPackage = metaClazz.getPackageName();
                    subEntityIsTypeParam = true;
                    break;
                }
            }
        }
    }

    public String getSubEntitySimpleName() {
        return subEntitySimpleName;
    }

    public TypeName getSubEntityTypeName() {
        return ClassName.get(subEntityPackage, subEntitySimpleName);
    }

    public boolean isSubEntityIsTypeParam() {
        return subEntityIsTypeParam;
    }

    public CblDefaultHolder getDefaultHolder() {
        return defaultHolder;
    }

    public boolean isTypeOfSubEntity() {
        return !StringUtils.isBlank(subEntitySimpleName);
    }

    @Override
    public MethodSpec getter(String dbName) {
        TypeName returnType = TypeUtil.parseMetaType(getMetaField().getType(), getSubEntitySimpleName());

        MethodSpec.Builder builder = MethodSpec.methodBuilder("get" + WordUtils.capitalize(getMetaField().getName())).
                addModifiers(Modifier.PUBLIC).
                returns(returnType);


        if (isTypeOfSubEntity()) {
            returnType = TypeUtil.parseMetaType(getMetaField().getType(), getSubEntitySimpleName());
            TypeName castType = isSubEntityIsTypeParam() ? TypeUtil.createListWithMapStringObject() : TypeUtil.createMapStringObject();
            builder.addStatement("return ($T) $T.fromMap(($T)mDoc.get($N))", returnType, getSubEntityTypeName(), castType, getConstantName());
        } else {
            builder.addStatement("return ($T) mDoc.get($N)", returnType, getConstantName());
        }

        return builder.build();
    }

    @Override
    public MethodSpec setter(String dbName, TypeName entityTypeName, boolean useMDocChanges) {
        TypeName fieldType = TypeUtil.parseMetaType(getMetaField().getType(), getSubEntitySimpleName());
        MethodSpec.Builder builder = MethodSpec.methodBuilder("set" + WordUtils.capitalize(getMetaField().getName())).
                addModifiers(Modifier.PUBLIC).
                addParameter(fieldType, "value").
                returns(entityTypeName);

        String docName = useMDocChanges ? "mDocChanges" : "mDoc";

        if (isTypeOfSubEntity()) {
            builder.addStatement("$N.put($N, $T.toMap(($T)value))", docName, getConstantName(), getSubEntityTypeName(), fieldType);
            builder.addStatement("return this");
        } else {
            builder.addStatement("$N.put($N, value)", docName, getConstantName());
            builder.addStatement("return this");
        }

        return builder.build();
    }

    @Override
    public List<FieldSpec> createFieldConstant() {

        FieldSpec fieldAccessorConstant = FieldSpec.builder(String.class, getConstantName(), Modifier.FINAL, Modifier.PUBLIC, Modifier.STATIC).
                initializer("$S", getDbField()).
                build();

        return Collections.singletonList(fieldAccessorConstant);
    }
}
