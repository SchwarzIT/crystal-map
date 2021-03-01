package com.kaufland.generation.mapper

import com.kaufland.ProcessingContext
import com.kaufland.ProcessingContext.isAssignable
import com.kaufland.model.mapper.MapifyHolder
import com.kaufland.model.mapper.MapperHolder
import com.kaufland.util.FieldExtractionUtil
import com.kaufland.util.TypeUtil
import com.squareup.kotlinpoet.*
import kaufland.com.coachbasebinderapi.mapify.Mapifyable
import java.lang.StringBuilder
import java.lang.reflect.Field

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

            if (!field.accessible) {
                typeSpec.addProperty(PropertySpec.builder(field.accessorName, Field::class.java.asTypeName())
                        .initializer("%T::class.java.getDeclaredField(%S).apply{isAccessible = true}", holder.sourceClazzTypeName, field.fieldName)
                        .build())


                typeSpec.addProperty(PropertySpec.builder(field.fieldName, field.typeName).receiver(holder.sourceClazzTypeName).mutable(true)
                        .getter(FunSpec.getterBuilder().addStatement("return %N.get(this) as %T", field.accessorName, field.typeName).build())
                        .setter(FunSpec.setterBuilder().addParameter("value", field.typeName).addStatement("%N.set(this, value)", field.accessorName).build())
                        .build())

            }
            val resolverParam = ResolverParam()
            resolveDeclaringName(field.declaringName, resolverParam)

            resolverParam?.apply {
                fromMap.addStatement("obj.%N = map[%S]?.let{${fromMapBuilder.toString()}}", field.fieldName, field.mapName, *fromMapParams.toTypedArray())
                toMap.addStatement("map[%S] =  obj.%N?.let{${toMapBuilder.toString()}}", field.mapName,field.fieldName, *toMapParams.toTypedArray())
            }


        }

        toMap.addStatement("return map")

        typeSpec.addFunction(fromMap.build())
        typeSpec.addFunction(toMap.build())

        return FileSpec.get(holder.`package`, typeSpec.build())
    }

    private data class ResolverParam(val fromMapBuilder: StringBuilder = StringBuilder(), val fromMapParams: MutableList<Any> = mutableListOf(), val toMapBuilder: StringBuilder = StringBuilder(), val toMapParams: MutableList<Any> = mutableListOf())

    private enum class ParentType {
        LIST,
        NONE
    }

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
                    resolverParam.fromMapBuilder.append("it.map{")
                    resolveDeclaringName(name.typeParams[0], resolverParam)
                    resolverParam.toMapBuilder.append("}")
                    resolverParam.fromMapBuilder.append("}")
                }
                isAssignable(Map::class.java) -> {
                    resolverParam.toMapBuilder.append("it.map{it.key.let{")
                    resolverParam.fromMapBuilder.append("it.map{it.key.let{")
                    resolveDeclaringName(name.typeParams[0], resolverParam)
                    resolverParam.toMapBuilder.append("} to it.value.let{")
                    resolverParam.fromMapBuilder.append("} to it.value.let{")
                    resolveDeclaringName(name.typeParams[1], resolverParam)
                    resolverParam.toMapBuilder.append("}}")
                    resolverParam.fromMapBuilder.append("}}.toMap()")
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