package com.kaufland.model;

import com.helger.jcodemodel.JClassAlreadyExistsException;
import com.helger.jcodemodel.JCodeModel;

interface GenerationModel {

    JCodeModel generateModel(JCodeModel model) throws JClassAlreadyExistsException, ClassNotFoundException;
}
