package com.kaufland;

import com.sun.tools.javac.code.Symbol;
import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaClass;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;

import kaufland.com.coachbasebinderapi.MapWrapper;
import kaufland.com.coachbasebinderapi.Entity;

public class ElementMetaModel {

    private Map<String, Element> cblEntityAnnotated;

    private Map<String, Element> cblChildEntityAnnotated;

    private JavaProjectBuilder builder = new JavaProjectBuilder();

    private Logger mLogger;

    public ElementMetaModel(RoundEnvironment roundEnv, Logger logger){
        mLogger = logger;
        cblEntityAnnotated = parse(roundEnv.getElementsAnnotatedWith(Entity.class));
        cblChildEntityAnnotated = parse(roundEnv.getElementsAnnotatedWith(MapWrapper.class));
    }

    private Map<String, Element> parse(Set<? extends Element> annotatedElements) {
        Map<String, Element> result = new HashMap<>();
        for (Element elem : annotatedElements) {
            result.put(elem.toString(), elem);
            try {
                builder.addSource(((Symbol.ClassSymbol) elem).sourcefile.openReader(false));
            } catch (IOException e) {
                mLogger.error("failed to add Element to meta source", elem);
            }
        }
        return result;
    }

    public Collection<Element> getEntityElements() {
        return cblEntityAnnotated.values();
    }

    public Collection<Element> getChildEntityElements() {
        return cblChildEntityAnnotated.values();
    }

    public boolean isMapWrapper(String clazzName){
        return cblChildEntityAnnotated.keySet().contains(clazzName);
    }

    public boolean isEntity(String clazzName){
        return cblEntityAnnotated.keySet().contains(clazzName);
    }

    public <T extends Element> JavaClass getMetaFor(T annotatedClass){
        return getMetaFor(annotatedClass.toString());
    }

    public JavaClass getMetaFor(String clazzName){
        return builder.getClassByName(clazzName);
    }
}
