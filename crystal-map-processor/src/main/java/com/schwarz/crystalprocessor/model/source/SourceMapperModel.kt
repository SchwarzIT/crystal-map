package com.schwarz.crystalprocessor.model.source

import com.schwarz.crystalapi.mapify.Mapify
import com.schwarz.crystalcore.model.mapper.Field
import com.schwarz.crystalcore.model.mapper.GetterSetter
import com.schwarz.crystalcore.model.source.IClassModel
import com.schwarz.crystalcore.model.source.ISourceDeclaringName
import com.schwarz.crystalcore.model.source.ISourceMapperModel
import com.schwarz.crystalprocessor.ProcessingContext
import com.schwarz.crystalprocessor.ProcessingContext.DeclaringName
import com.schwarz.crystalprocessor.util.ElementUtil
import com.sun.tools.javac.code.Symbol
import com.sun.tools.javac.code.Type
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.type.TypeMirror

class SourceMapperModel(source: Element) :
    IClassModel<Element> by SourceClassModel(source), ISourceMapperModel<Element> {

    override val typeParams: List<String> =
        (source as Symbol.ClassSymbol).typeParameters.mapNotNull {
            ElementUtil.splitGenericIfNeeded(it.asType().toString())[0]
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
                    fields[childElement.simpleName.toString()] = Field(SourceClassModel(childElement), SourceMapify(this))
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
                                mapify = SourceMapify(anno)
                            }
                        } else if (it.startsWith("get")) {
                            getterSetters.getOrPut(it.substringAfter("get")) {
                                GetterSetter()
                            }.apply {
                                getterElement = SourceGetterSetterModel(childElement as Symbol.MethodSymbol)
                                mapify = SourceMapify(anno)
                            }
                        }
                        null
                    }
                }
            }
        }
    }
}
