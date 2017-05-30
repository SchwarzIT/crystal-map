package com.kaufland;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.management.RuntimeErrorException;
import javax.tools.Diagnostic;

/**
 * Created by sbra0902 on 24.05.17.
 */

public class Logger {

    private static Logger currentInstance;

    private final Messager messager;

    Logger(ProcessingEnvironment processingEnv) {
        this.messager = processingEnv.getMessager();
        currentInstance = this;
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
        messager.printMessage(Diagnostic.Kind.ERROR, msg, e);
    }

    public void abortWithError(String msg, Element e) {
        error(msg, e);
        throw new RuntimeException();
    }

    public static Logger getInstance() {
        return currentInstance;
    }
}
