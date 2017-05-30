package com.kaufland;

import com.helger.jcodemodel.JCodeModel;
import com.kaufland.generation.CodeGenerator;
import com.kaufland.model.EntityGenModel;

import java.io.IOException;
import java.util.HashSet;
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

    private JCodeModel mCodeModel;

    private CodeGenerator mCodeGenerator;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        mLogger = new Logger(processingEnvironment);
        mCodeModel = new JCodeModel();
        mCodeGenerator = new CodeGenerator(processingEnvironment.getFiler(), "TODO");
        super.init(processingEnvironment);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnv) {
        for (Element elem : roundEnv.getElementsAnnotatedWith(CblEntity.class)) {

            try {
                mCodeGenerator.generate(new EntityGenModel(elem), elem);
            } catch (IOException e) {
                mLogger.abortWithError("generation failed", elem);
            }

        }
        return true; // no further processing of this annotation type
    }

}
