package com.kaufland.model.source;

import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.JDirectClass;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sbra0902 on 02.06.17.
 */

public class CblEntityHolder {

    private List<CblFieldHolder> mFields = new ArrayList<>();

    private AbstractJClass sourceClazz;



    public List<CblFieldHolder> getFields() {
        return mFields;
    }

    public void setFields(List<CblFieldHolder> fields) {
        mFields = fields;
    }

    public AbstractJClass getSourceClazz() {
        return sourceClazz;
    }

    public void setSourceClazz(AbstractJClass sourceClazz) {
        this.sourceClazz = sourceClazz;
    }
}
