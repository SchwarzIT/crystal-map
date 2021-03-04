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


            resolverParam?.apply {
                resolverParam.fromMapBuilder.beginControlFlow("map[%S]?.let", field.mapName)
                resolverParam.toMapBuilder.beginControlFlow("map[%S]·=·obj.%N?.let",  field.mapName, field.accessorName)
                resolveDeclaringName(field.declaringName, resolverParam)
                resolverParam.fromMapBuilder.endControlFlow()
                resolverParam.fromMapBuilder.beginControlFlow("?.apply")
                resolverParam.fromMapBuilder.addStatement("obj.%N=this",  field.accessorName)
                resolverParam.fromMapBuilder.endControlFlow()

                resolverParam.toMapBuilder.endControlFlow()

                fromMap.addCode(resolverParam.fromMapBuilder.build())
                toMap.addCode(resolverParam.toMapBuilder.build())
            }


        }

        toMap.addStatement("return map")

        typeSpec.addFunction(fromMap.build())
        typeSpec.addFunction(toMap.build())

        return FileSpec.get(holder.`package`, typeSpec.build())
    }

    private data class ResolverParam(val fromMapBuilder: CodeBlock.Builder = CodeBlock.builder(), val toMapBuilder: CodeBlock.Builder = CodeBlock.builder())

    private fun resolveDeclaringName(name: ProcessingContext.DeclaringName, resolverParam: ResolverParam) {

        if (name.isProcessingType()) {
            resolverParam.fromMapBuilder.addStatement("%T.Mapper().fromMap(it as %T)", name.asTypeName()!!, TypeUtil.mapStringAny())

            resolverParam.toMapBuilder.addStatement("%T.Mapper().toMap(it)", name.asTypeName()!!)
            return
        }

        if (name.isPlainType()) {
            name.asTypeName()?.let {
                resolverParam.fromMapBuilder.addStatement("it as %T", it)

                resolverParam.toMapBuilder.addStatement("it")
            }
            return
        }

        name.asTypeElement()?.apply {
            when {
                isAssignable(List::class.java) -> {
                    resolverParam.toMapBuilder.beginControlFlow("it.map")
                    resolverParam.fromMapBuilder.beginControlFlow("(it as? %T)?.map", TypeUtil.list(TypeUtil.any()))
                    resolveDeclaringName(name.typeParams[0], resolverParam)
                    resolverParam.toMapBuilder.endControlFlow()
                    resolverParam.fromMapBuilder.endControlFlow()
                }
                isAssignable(Map::class.java) -> {
                    resolverParam.toMapBuilder.beginControlFlow("it.map")
                    resolverParam.toMapBuilder.beginControlFlow("(it.key.let")
                    resolverParam.fromMapBuilder.beginControlFlow("(it as? %T)?.map", TypeUtil.mapAnyAny())
                    resolverParam.fromMapBuilder.beginControlFlow("(it.key.let")
                    resolveDeclaringName(name.typeParams[0], resolverParam)
                    resolverParam.toMapBuilder.endControlFlow()
                    resolverParam.toMapBuilder.beginControlFlow(" to it.value.let")
                    resolverParam.fromMapBuilder.endControlFlow()
                    resolverParam.fromMapBuilder.beginControlFlow(" to it.value.let")
                    resolveDeclaringName(name.typeParams[1], resolverParam)

                    resolverParam.toMapBuilder.endControlFlow()
                    resolverParam.toMapBuilder.addStatement(")")
                    resolverParam.toMapBuilder.endControlFlow()
                    resolverParam.fromMapBuilder.endControlFlow()
                    resolverParam.fromMapBuilder.addStatement(")")
                    resolverParam.fromMapBuilder.endControlFlow()
                    resolverParam.fromMapBuilder.addStatement("?.toMap()")
                }
                isAssignable(Serializable::class.java) -> {
                    resolverParam.fromMapBuilder.addStatement("%T().fromMap(it as %T)", TypeUtil.serializableMapifyable(name.asTypeName()!!), TypeUtil.mapStringAny())
                    resolverParam.toMapBuilder.addStatement("%T().toMap(it)", TypeUtil.serializableMapifyable(name.asTypeName()!!))
                }
                else -> {
                    FieldExtractionUtil.typeMirror(getAnnotation(Mapifyable::class.java))?.apply {
                        val fullTypeName = ProcessingContext.DeclaringName(toString()).asFullTypeName()
                        resolverParam.fromMapBuilder.addStatement("%T().fromMap(it as %T)", fullTypeName!!, TypeUtil.mapStringAny())

                        resolverParam.toMapBuilder.addStatement("%T().toMap(it)", fullTypeName!!)
                    }
                }
            }
        }
    }
}