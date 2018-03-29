package com.kaufland;

import com.kaufland.generation.CodeGenerator;
import com.kaufland.model.EntityGeneration;
import com.kaufland.model.source.CblEntityHolder;
import com.kaufland.model.source.SourceContentParser;
import com.kaufland.model.validation.CblEntityValidator;
import com.squareup.javapoet.JavaFile;
import com.sun.tools.javac.code.Symbol;
import com.thoughtworks.qdox.JavaProjectBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import kaufland.com.coachbasebinderapi.CblEntity;

@SupportedAnnotationTypes({"kaufland.com.coachbasebinderapi.CblField", "kaufland.com.coachbasebinderapi.CblEntity"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class CoachBaseBinderProcessor extends AbstractProcessor {

    private Logger mLogger;

    private List<JavaFile> mFilesToGenerate;

    private CodeGenerator mCodeGenerator;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        mLogger = new Logger(processingEnvironment);
        mFilesToGenerate = new ArrayList<>();
        mCodeGenerator = new CodeGenerator(processingEnvironment.getFiler());
        super.init(processingEnvironment);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnv) {

        Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(CblEntity.class);

        Map<String, Element> cblFieldAnnotated = new HashMap<>();
        JavaProjectBuilder builder = new JavaProjectBuilder();

        for (Element elem : annotatedElements) {
            cblFieldAnnotated.put(elem.getSimpleName().toString(), elem);
            try {
                builder.addSource(((Symbol.ClassSymbol) elem).sourcefile.openReader(false));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        SourceContentParser parser = new SourceContentParser();
        CblEntityValidator validator = new CblEntityValidator();
        EntityGeneration generation = new EntityGeneration();

        for (Element elem : annotatedElements) {
            try {
                CblEntityHolder holder = parser.parse(elem, cblFieldAnnotated, builder.getClassByName(elem.toString()));

                validator.validate(holder, mLogger);

                if (!mLogger.hasErrors()) {
                    JavaFile entityFile = generation.generateModel(holder);
                    mCodeGenerator.generate(entityFile);
                }

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                mLogger.abortWithError("Clazz not found", parser.getCurrentWorkingObject() != null ? parser.getCurrentWorkingObject() : elem);
            }
            catch (IOException e) {
                mLogger.abortWithError("generation failed", elem);
            }
        }

        return true; // no further processing of this annotation type
    }

}
