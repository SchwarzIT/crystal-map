package com.kaufland.model.field;

import com.kaufland.util.ConversionUtil;
import com.squareup.javapoet.CodeBlock;
import com.thoughtworks.qdox.model.JavaField;

/**
 * Created by sbra0902 on 21.06.17.
 */

public class CblDefaultHolder{

    private String defaultValue;

    public CblDefaultHolder(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

}
