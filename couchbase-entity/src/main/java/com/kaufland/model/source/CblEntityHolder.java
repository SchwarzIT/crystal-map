package com.kaufland.model.source;

import com.helger.jcodemodel.AbstractJClass;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Element;

public class CblEntityHolder {

    private List<CblBaseFieldHolder> mFields = new ArrayList<>();

    private AbstractJClass sourceClazz;

    private Element sourceElement;


    public List<CblBaseFieldHolder> getFields() {
        return mFields;
    }

    public AbstractJClass getSourceClazz() {
        return sourceClazz;
    }

    public void setSourceClazz(AbstractJClass sourceClazz) {
        this.sourceClazz = sourceClazz;
    }

    public Element getSourceElement() {
        return sourceElement;
    }

    public void setSourceElement(Element sourceElement) {
        this.sourceElement = sourceElement;
    }
}
