package com.schwarz.crystalksp.model.source

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.schwarz.crystalapi.mapify.Mapify
import com.schwarz.crystalcore.model.mapper.Field
import com.schwarz.crystalcore.model.mapper.GetterSetter
import com.schwarz.crystalcore.model.source.IClassModel
import com.schwarz.crystalcore.model.source.ISourceMapperModel
import com.schwarz.crystalksp.ProcessingContext
import com.schwarz.crystalksp.util.getAnnotation

class SourceMapperModel(source: KSClassDeclaration) :
    IClassModel<KSNode> by SourceClassModel(source), ISourceMapperModel<KSNode> {

    override val typeParams: List<String> = source.typeParameters.map {
        it.name.getShortName()
    }.toList()

    override val declaringName = ProcessingContext.DeclaringName(source)
    override val fields: HashMap<String, Field<KSNode>> = HashMap()

    override val getterSetters: HashMap<String, GetterSetter<KSNode>> = HashMap()

    init {
        for (function in source.getAllFunctions()) {
//            if (function.modifiers.contains(Modifier.STATIC)) {
//                continue
//            }

            function.getAnnotation(Mapify::class)?.let { anno ->
                function.simpleName.toString()?.let {
                    if (it.startsWith("set")) {
                        getterSetters.getOrPut(it.substringAfter("set")) {
                            GetterSetter()
                        }.apply {
                            setterElement = SourceGetterSetterModel(function)
                            mapify = SourceMapify(anno)
                        }
                    } else if (it.startsWith("get")) {
                        getterSetters.getOrPut(it.substringAfter("get")) {
                            GetterSetter()
                        }.apply {
                            getterElement = SourceGetterSetterModel(function)
                            mapify = SourceMapify(anno)
                        }
                    }
                    null
                }
            }
        }

        for (field in source.getAllProperties()) {
            field.getAnnotation(Mapify::class)?.apply {
                fields[field.simpleName.asString()] = Field(SourceClassModel(field), SourceMapify(this))
            }
        }
    }
}
