package com.schwarz.crystalprocessor.model.source

import com.schwarz.crystalapi.mapify.Mapify
import com.schwarz.crystalcore.model.mapper.Field
import com.schwarz.crystalcore.model.mapper.GetterSetter
import com.schwarz.crystalcore.model.source.IClassModel
import com.schwarz.crystalcore.model.source.ISourceDeclaringName
import com.schwarz.crystalcore.model.source.ISourceMapper
import com.schwarz.crystalprocessor.ProcessingContext
import com.sun.tools.javac.code.Symbol
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier

class SourceMapper(source: Element) :
    IClassModel<Element> by SourceClassModel(source), ISourceMapper<Element> {

    override val typeParams: List<ISourceDeclaringName> =
        (source as Symbol.ClassSymbol).typeParameters.mapNotNull {
            ProcessingContext.DeclaringName(it.asType())
        }

    override val declaringName = ProcessingContext.DeclaringName(source.asType())
    override val fields: HashMap<String, Field<Element>> = HashMap()

    override val getterSetters: HashMap<String, GetterSetter<Element>> = HashMap()

    init {
        for (childElement in source.enclosedElements) {
            if (childElement.modifiers.contains(Modifier.STATIC)) {
                continue
            }

            if (childElement.kind == ElementKind.FIELD) {
                childElement.getAnnotation(Mapify::class.java)?.apply {
                    fields[childElement.simpleName.toString()] = Field(SourceClassModel(childElement), this)
                }
            }

            if (childElement.kind == ElementKind.METHOD) {
                childElement.getAnnotation(Mapify::class.java)?.let { anno ->
                    childElement.simpleName.toString()?.let {
                        if (it.startsWith("set")) {
                            getterSetters.getOrPut(it.substringAfter("set")) {
                                GetterSetter()
                            }.apply {
                                setterElement = SourceGetterSetterModel(childElement as Symbol.MethodSymbol)
                                mapify = anno
                            }
                        } else if (it.startsWith("get")) {
                            getterSetters.getOrPut(it.substringAfter("get")) {
                                GetterSetter()
                            }.apply {
                                getterElement = SourceGetterSetterModel(childElement as Symbol.MethodSymbol)
                                mapify = anno
                            }
                        }
                        null
                    }
                }
            }
        }
    }
}
