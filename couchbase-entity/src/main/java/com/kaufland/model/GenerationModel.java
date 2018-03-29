package com.kaufland.model;

import com.kaufland.model.source.CblEntityHolder;
import com.squareup.javapoet.JavaFile;

interface GenerationModel {

    JavaFile generateModel(CblEntityHolder holder);
}
