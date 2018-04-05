package com.kaufland.model.field;

import com.kaufland.ElementMetaModel;
import com.kaufland.util.TypeUtil;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
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

    private String subEntityName;

    private boolean subEntityIsTypeParam;

    private CblDefaultHolder defaultHolder;

    public CblFieldHolder(CblField field, Element fieldElement, JavaField metaField, CblDefaultHolder defaultHolder, ElementMetaModel metaModel) {
        super(field.value(), fieldElement, metaField);
        this.defaultHolder = defaultHolder;

        String typeName = metaField.getType().getCanonicalName();
        if (metaModel.isChildEntity(typeName)) {
            subEntityName = typeName + "Entity";
        } else if (metaField.getType() instanceof DefaultJavaParameterizedType) {
            for (JavaType typeParameter : ((DefaultJavaParameterizedType) metaField.getType()).getActualTypeArguments()) {

                String simpleName = typeParameter.getCanonicalName();
                if (metaModel.isChildEntity(simpleName)) {
                    subEntityName = simpleName + "Entity";
                    subEntityIsTypeParam = true;
                    break;
                }
            }
        }
    }

    public String getSubEntityName() {
        return subEntityName;
    }

    public boolean isSubEntityIsTypeParam() {
        return subEntityIsTypeParam;
    }

    public CblDefaultHolder getDefaultHolder() {
        return defaultHolder;
    }

    public boolean isTypeOfSubEntity() {
        return !StringUtils.isBlank(subEntityName);
    }

    @Override
    public MethodSpec getter(String dbName) {
        TypeName returnType = TypeUtil.parseMetaType(getMetaField().getType(), getSubEntityName());

        MethodSpec.Builder builder = MethodSpec.methodBuilder("get" + WordUtils.capitalize(getMetaField().getName())).
                addModifiers(Modifier.PUBLIC).
                returns(returnType);


        if (isTypeOfSubEntity()) {
            returnType = TypeUtil.parseMetaType(getMetaField().getType(), getSubEntityName());
            TypeName castType = isSubEntityIsTypeParam() ? TypeUtil.createListWithMapStringObject() : TypeUtil.createMapStringObject();
            builder.addStatement("return ($T) $N.fromMap(($T)mDoc.get($N))", returnType, getSubEntityName(), castType, getConstantName());
        } else {
            builder.addStatement("return ($T) mDoc.get($N)", returnType, getConstantName());
        }

        return builder.build();
    }

    @Override
    public MethodSpec setter(String dbName, TypeName entityTypeName, boolean useMDocChanges) {
        TypeName fieldType = TypeUtil.parseMetaType(getMetaField().getType(), getSubEntityName());
        MethodSpec.Builder builder = MethodSpec.methodBuilder("set" + WordUtils.capitalize(getMetaField().getName())).
                addModifiers(Modifier.PUBLIC).
                addParameter(fieldType, "value").
                returns(entityTypeName);

        String docName = useMDocChanges ? "mDocChanges" : "mDoc";

        if (isTypeOfSubEntity()) {
            builder.addStatement("$N.put($N, $N.toMap(($T)value))", docName, getConstantName(), getSubEntityName(), fieldType);
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
