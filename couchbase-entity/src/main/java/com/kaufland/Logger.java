package com.kaufland;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

public class Logger {


    private final Messager messager;

    private boolean hasErrors;

    Logger(ProcessingEnvironment processingEnv) {
        this.messager = processingEnv.getMessager();
    }

    public void info(String msg) {
        messager.printMessage(Diagnostic.Kind.NOTE, msg);
    }

    public void info(String msg, Element e) {
        messager.printMessage(Diagnostic.Kind.NOTE, msg, e);
    }

    public void warn(String msg, Element e) {
        messager.printMessage(Diagnostic.Kind.WARNING, msg, e);
    }

    public void error(String msg, Element e) {
        hasErrors = true;
        messager.printMessage(Diagnostic.Kind.ERROR, msg, e);
    }

    public void abortWithError(String msg, Element e, Throwable ex) {
        error(msg, e);
        throw new RuntimeException(ex);
    }

    public boolean hasErrors() {
        return hasErrors;
    }
}
