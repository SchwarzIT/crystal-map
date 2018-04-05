package com.kaufland;

import com.squareup.javapoet.JavaFile;

import java.util.Collection;

import javax.lang.model.element.Element;

public interface EntityProcessor {

    JavaFile process(Element element);

}
