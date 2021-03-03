package com.kaufland.generation.mapper

import com.kaufland.ProcessingContext
import com.kaufland.ProcessingContext.isAssignable
import com.kaufland.model.mapper.MapperHolder
import com.kaufland.util.FieldExtractionUtil
import com.kaufland.util.TypeUtil
import com.squareup.kotlinpoet.*
import kaufland.com.coachbasebinderapi.mapify.Mapifyable
import kaufland.com.coachbasebinderapi.util.SerializableMapifyable
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.lang.StringBuilder
import java.lang.reflect.Field
import java.util.*

class MapperGeneration {

    fun generate(holder: MapperHolder): FileSpec {


        val typeSpec = TypeSpec.classBuilder(holder.targetMapperSimpleName)
                .addSuperinterface(TypeUtil.iMapper(holder.sourceClazzTypeName))


        val fromMap = FunSpec.builder("fromMap")
                .addModifiers(KModifier.OVERRIDE)
                .addParameter("obj", holder.sourceClazzTypeName)
                .addParameter("map", TypeUtil.mapStringAny())

        val toMap = FunSpec.builder("toMap")
                .addModifiers(KModifier.OVERRIDE)
                .addParameter("obj", holder.sourceClazzTypeName)
                .returns(TypeUtil.mapStringAny())
                .addStatement("val map = %T()", TypeUtil.hashMapStringAny())



        for (field in holder.fields.values) {

            typeSpec.addProperty(PropertySpec.builder(field.reflectedFieldName, Field::class.java.asTypeName(), KModifier.PRIVATE)
                    .initializer("%T::class.java.getDeclaredField(%S).apply{isAccessible = true}", holder.sourceClazzTypeName, field.fieldName)
                    .build())


            typeSpec.addProperty(PropertySpec.builder(field.accessorName, field.typeName, KModifier.PRIVATE).receiver(holder.sourceClazzTypeName).mutable(true)
                    .getter(FunSpec.getterBuilder().addStatement("return %N.get(this) as %T", field.reflectedFieldName, field.typeName).build())
                    .setter(FunSpec.setterBuilder().addParameter("value", field.typeName).addStatement("%N.set(this, value)", field.reflectedFieldName).build())
                    .build())

            val resolverParam = ResolverParam()
            resolveDeclaringName(field.declaringName, resolverParam)

            resolverParam?.apply {
                fromMap.addCode("map[%S]?.let{${fromMapBuilder.toString()}}?.apply{obj.%N=this}", field.mapName, *fromMapParams.toTypedArray(), field.accessorName)
                toMap.addCode("map[%S]·=·obj.%N?.let{${toMapBuilder.toString()}}", field.mapName, field.accessorName, *toMapParams.toTypedArray())
            }


        }

        toMap.addStatement("return map")

        typeSpec.addFunction(fromMap.build())
        typeSpec.addFunction(toMap.build())

        return FileSpec.get(holder.`package`, typeSpec.build())
    }

    private data class ResolverParam(val fromMapBuilder: StringBuilder = StringBuilder(), val fromMapParams: MutableList<Any> = mutableListOf(), val toMapBuilder: StringBuilder = StringBuilder(), val toMapParams: MutableList<Any> = mutableListOf())

    private fun resolveDeclaringName(name: ProcessingContext.DeclaringName, resolverParam: ResolverParam) {

        if (name.isProcessingType()) {
            resolverParam.fromMapBuilder.append("%T.Mapper().fromMap(it as %T)")
            resolverParam.fromMapParams.addAll(listOf(name.asTypeName()!!, TypeUtil.mapStringAny()))

            resolverParam.toMapBuilder.append("%T.Mapper().toMap(it)")
            resolverParam.toMapParams.add(name.asTypeName()!!)
            return
        }

        if (name.isPlainType()) {
            name.asTypeName()?.let {
                resolverParam.fromMapBuilder.append("it as %T")
                resolverParam.fromMapParams.add(it)

                resolverParam.toMapBuilder.append("it")
            }
            return
        }

        name.asTypeElement()?.apply {
            when {
                isAssignable(List::class.java) -> {
                    resolverParam.toMapBuilder.append("it.map{")
                    resolverParam.fromMapBuilder.append("(it as? %T)?.map{")
                    resolverParam.fromMapParams.add(TypeUtil.list(TypeUtil.any()))
                    resolveDeclaringName(name.typeParams[0], resolverParam)
                    resolverParam.toMapBuilder.append("}")
                    resolverParam.fromMapBuilder.append("}")
                }
                isAssignable(Map::class.java) -> {
                    resolverParam.toMapBuilder.append("it.map{it.key.let{")
                    resolverParam.fromMapBuilder.append("(it as? %T)?.map{it.key.let{")
                    resolverParam.fromMapParams.add(TypeUtil.mapAnyAny())
                    resolveDeclaringName(name.typeParams[0], resolverParam)
                    resolverParam.toMapBuilder.append("} to it.value.let{")
                    resolverParam.fromMapBuilder.append("} to it.value.let{")
                    resolveDeclaringName(name.typeParams[1], resolverParam)
                    resolverParam.toMapBuilder.append("}}")
                    resolverParam.fromMapBuilder.append("}}?.toMap()")
                }
                isAssignable(Serializable::class.java) -> {
                    resolverParam.fromMapBuilder.append("%T().fromMap(it as %T)")
                    resolverParam.fromMapParams.addAll(listOf(TypeUtil.serializableMapifyable(name.asTypeName()!!), TypeUtil.mapStringAny()))

                    resolverParam.toMapBuilder.append("%T().toMap(it)")
                    resolverParam.toMapParams.add(TypeUtil.serializableMapifyable(name.asTypeName()!!))
                }
                else -> {
                    FieldExtractionUtil.typeMirror(getAnnotation(Mapifyable::class.java))?.apply {
                        val fullTypeName = ProcessingContext.DeclaringName(toString()).asFullTypeName()
                        resolverParam.fromMapBuilder.append("%T().fromMap(it as %T)")
                        resolverParam.fromMapParams.addAll(listOf(fullTypeName!!, TypeUtil.mapStringAny()))

                        resolverParam.toMapBuilder.append("%T().toMap(it)")
                        resolverParam.toMapParams.add(fullTypeName!!)
                    }
                }
            }
        }
    }
}