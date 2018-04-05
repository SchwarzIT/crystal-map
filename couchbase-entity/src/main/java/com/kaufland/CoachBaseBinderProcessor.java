package com.kaufland;

import com.kaufland.generation.ChildEntityGeneration;
import com.kaufland.generation.CodeGenerator;
import com.kaufland.generation.EntityGeneration;
import com.kaufland.model.entity.CblChildEntityHolder;
import com.kaufland.model.entity.CblEntityHolder;
import com.kaufland.model.EntityFactory;
import com.kaufland.validation.CblEntityValidator;
import com.squareup.javapoet.JavaFile;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

@SupportedAnnotationTypes({"kaufland.com.coachbasebinderapi.CblField", "kaufland.com.coachbasebinderapi.CblEntity"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class CoachBaseBinderProcessor extends AbstractProcessor {

    private Logger mLogger;

    private CodeGenerator mCodeGenerator;

    private CblEntityValidator validator;

    private ElementMetaModel mElementMetaModel;


    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        mLogger = new Logger(processingEnvironment);
        mCodeGenerator = new CodeGenerator(processingEnvironment.getFiler());
        validator = new CblEntityValidator();
        super.init(processingEnvironment);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnv) {

        mElementMetaModel = new ElementMetaModel(roundEnv, mLogger);

        validateAndProcess(mElementMetaModel.getEntityElements(), new EntityProcessor() {
            @Override
            public JavaFile process(Element element) {
                CblEntityHolder holder = EntityFactory.createEntityHolder(element, mElementMetaModel);
                return new EntityGeneration().generateModel(holder);
            }
        });


        validateAndProcess(mElementMetaModel.getChildEntityElements(), new EntityProcessor() {
            @Override
            public JavaFile process(Element element) {
                CblChildEntityHolder holder = EntityFactory.createChildEntityHolder(element, mElementMetaModel);
                return new ChildEntityGeneration().generateModel(holder);
            }
        });

        return true; // no further processing of this annotation type
    }

    private void validateAndProcess(Collection<Element> elements, EntityProcessor processor) {
        for (Element elem : elements) {
            try {
                validator.validate(elem, mElementMetaModel, mLogger);

                if (!mLogger.hasErrors()) {
                    JavaFile entityFile = processor.process(elem);
                    mCodeGenerator.generate(entityFile);
                }

            } catch (ClassNotFoundException e) {
                mLogger.abortWithError("Clazz not found", elem);
            } catch (Exception e) {
                e.printStackTrace();
                mLogger.abortWithError("generation failed", elem);
            }
        }
    }


}
