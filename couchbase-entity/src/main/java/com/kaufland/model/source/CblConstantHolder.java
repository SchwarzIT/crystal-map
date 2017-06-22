package com.kaufland.model.source;

import com.helger.jcodemodel.AbstractJClass;

import javax.lang.model.element.Element;

/**
 * Created by sbra0902 on 21.06.17.
 */

public class CblConstantHolder extends CblBaseFieldHolder{

    private String constantValue;


    public String getConstantValue() {
        return constantValue;
    }

    public void setConstantValue(String constantValue) {
        this.constantValue = constantValue;
    }
}
