package com.kaufland

import com.google.auto.service.AutoService
import com.kaufland.generation.CodeGenerator
import com.kaufland.generation.EntityGeneration
import com.kaufland.generation.WrapperGeneration
import com.kaufland.model.EntityFactory
import com.kaufland.validation.PreValidator
import com.squareup.kotlinpoet.FileSpec
import kaufland.com.coachbasebinderapi.Entity
import kaufland.com.coachbasebinderapi.MapWrapper
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.swing.UIManager.put



@SupportedAnnotationTypes("kaufland.com.coachbasebinderapi.Field", "kaufland.com.coachbasebinderapi.Entity", "kaufland.com.coachbasebinderapi.MapWrapper")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor::class)
class CoachBaseBinderProcessor : AbstractProcessor() {

    private var mLogger: Logger? = null

    private var mCodeGenerator: CodeGenerator? = null

    private var validator: PreValidator? = null


    @Synchronized
    override fun init(processingEnvironment: ProcessingEnvironment) {
        mLogger = Logger(processingEnvironment)
        mCodeGenerator = CodeGenerator(processingEnvironment.filer)
        validator = PreValidator()
        super.init(processingEnvironment)
    }

    override fun process(set: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {


        var mapWrappers = roundEnv.getElementsAnnotatedWith(MapWrapper::class.java)
        var mapWrapperStrings = mapWrappers.map { element -> element.toString() }

        validateAndProcess(roundEnv.getElementsAnnotatedWith(Entity::class.java), object : EntityProcessor {
            override fun process(element: Element): FileSpec {
                val holder = EntityFactory.createEntityHolder(element, mapWrapperStrings.contains(element.toString()))
                return EntityGeneration().generateModel(holder)
            }
        })


        validateAndProcess(mapWrappers, object : EntityProcessor {
            override fun process(element: Element): FileSpec {
                val holder = EntityFactory.createChildEntityHolder(element, mapWrapperStrings.contains(element.toString()))
                return WrapperGeneration().generateModel(holder)
            }
        })

        return true // no further processing of this annotation type
    }

    private fun validateAndProcess(elements: Collection<Element>, processor: EntityProcessor) {
        for (elem in elements) {
            try {
                validator!!.validate(elem, mLogger!!)

                if (!mLogger!!.hasErrors()) {
                    val entityFile = processor.process(elem)
                    mCodeGenerator!!.generate(entityFile)
                }

            } catch (e: ClassNotFoundException) {
                mLogger!!.abortWithError("Clazz not found", elem, e)
            } catch (e: Exception) {
                e.printStackTrace()
                mLogger!!.abortWithError("generation failed", elem, e)
            }

        }
    }


}
