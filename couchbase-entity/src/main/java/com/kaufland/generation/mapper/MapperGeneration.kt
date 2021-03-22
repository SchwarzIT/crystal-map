package com.kaufland.generation.mapper

import com.kaufland.ProcessingContext
import com.kaufland.ProcessingContext.isAssignable
import com.kaufland.generation.MapifyableImplGeneration
import com.kaufland.model.mapper.MapifyHolder
import com.kaufland.model.mapper.MapperHolder
import com.kaufland.util.FieldExtractionUtil
import com.kaufland.util.TypeUtil
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import kaufland.com.coachbasebinderapi.mapify.Mapifyable
import kaufland.com.coachbasebinderapi.mapify.Mapper
import java.io.Serializable
import java.lang.reflect.Field
import java.util.*

class MapperGeneration {

    fun generate(holder: MapperHolder): FileSpec {

        val mapperTypeParam = holder.declaringName.asFullTypeName() ?: holder.sourceClazzTypeName

        val typeSpec = TypeSpec.classBuilder(holder.targetMapperSimpleName)
                .addSuperinterface(TypeUtil.iMapper(mapperTypeParam))


        if (holder.typeParams.isNotEmpty()) {
            val constructorBuilder = FunSpec.constructorBuilder()

            holder.typeParams.forEachIndexed { index, typeVariableSymbol ->
                typeSpec.addTypeVariable(TypeVariableName(typeVariableSymbol.name))
                typeSpec.addProperty(PropertySpec.builder("typeParam$index", TypeUtil.iMapifyable(TypeVariableName(typeVariableSymbol.name)))
                        .initializer("typeParam$index")
                        .addModifiers(KModifier.PRIVATE)
                        .build())
                constructorBuilder.addParameter("typeParam$index", TypeUtil.iMapifyable(TypeVariableName(typeVariableSymbol.name)))
            }
            typeSpec.primaryConstructor(constructorBuilder.build())
        }


        val fromMap = FunSpec.builder("fromMap")
                .addModifiers(KModifier.OVERRIDE)
                .addParameter("obj", mapperTypeParam)
                .addParameter("map", TypeUtil.mapStringAny())

        val toMap = FunSpec.builder("toMap")
                .addModifiers(KModifier.OVERRIDE)
                .addParameter("obj", mapperTypeParam)
                .returns(TypeUtil.mapStringAny())
                .addStatement("val map = %T()", TypeUtil.hashMapStringAny())


        val addedHelpers = mutableSetOf<String>()
        for (mapifyHelper in holder.fields.values.filter { it.typeHandleMode == MapifyHolder.TypeHandleMode.MAPPER && it.declaringName.typeParams.isNotEmpty() }.map { it.declaringName.typeParams }.flatten()) {
            val helperClazzName = buildHelperClazzName(mapifyHelper)

            if (addedHelpers.contains(helperClazzName)) {
                continue
            }
            addedHelpers.add(helperClazzName)

            val param = ResolverParam()

            param.fromMapBuilder.beginControlFlow("return map[%S].let", "value")
            param.toMapBuilder.addStatement("val map = %T()", TypeUtil.hashMapStringAny())
            param.toMapBuilder.beginControlFlow("obj?.let")
            resolveDeclaringName(mapifyHelper, param, "", emptyList())
            param.fromMapBuilder.endControlFlow()
            param.toMapBuilder.endControlFlow()
            param.toMapBuilder.beginControlFlow("?.apply")
            param.toMapBuilder.addStatement("map[%S] = this", "value")
            param.toMapBuilder.endControlFlow()
            param.toMapBuilder.addStatement("return map")




            typeSpec.addType(MapifyableImplGeneration.typeSpec(MapifyableImplGeneration.Config(
                    modifiers = arrayOf(KModifier.PRIVATE),
                    clazzName = helperClazzName,
                    typeParam = mapifyHelper.asFullTypeName()!!,
                    fromMap = { it.addCode(param.fromMapBuilder.build()).build() },
                    toMap = { it.addCode(param.toMapBuilder.build()).build() })))
        }


        for (field in holder.fields.values) {

            typeSpec.addProperties(field.reflectionProperties(holder.sourceClazzTypeName))

            typeSpec.addProperty(PropertySpec.builder(field.accessorName, field.declaringName.asFullTypeName()
                    ?: field.typeName, KModifier.PRIVATE).receiver(mapperTypeParam).mutable(true)
                    .getter(field.getterFunSpec())
                    .setter(field.setterFunSpec())
                    .build())

            val resolverParam = ResolverParam()


            resolverParam?.apply {
                resolverParam.fromMapBuilder.beginControlFlow("map[%S]?.let", field.mapName)
                resolverParam.toMapBuilder.beginControlFlow("obj.%N?.let", field.accessorName)
                resolveDeclaringName(field.declaringName, resolverParam, field.accessorName, holder.typeParams)
                resolverParam.fromMapBuilder.endControlFlow()
                resolverParam.fromMapBuilder.beginControlFlow("?.apply")
                resolverParam.fromMapBuilder.addStatement("obj.%N=this", field.accessorName)
                resolverParam.fromMapBuilder.endControlFlow()
                resolverParam.toMapBuilder.endControlFlow()
                resolverParam.toMapBuilder.beginControlFlow("?.apply")
                resolverParam.toMapBuilder.addStatement("map[%S]=this", field.mapName)
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

    private fun buildHelperClazzName(name: ProcessingContext.DeclaringName): String {
        return "Helper${name.name.split('.').map { it.capitalize() }.joinToString(separator = "")}"
    }

    private data class ResolverParam(val fromMapBuilder: CodeBlock.Builder = CodeBlock.builder(), val toMapBuilder: CodeBlock.Builder = CodeBlock.builder())

    private fun resolveDeclaringName(name: ProcessingContext.DeclaringName, resolverParam: ResolverParam, accessorName: String, typeParams: List<ProcessingContext.DeclaringName>) {

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

        if (name.isTypeVar()) {

            typeParams.indexOfFirst { it.name == name.name }?.let {
                resolverParam.fromMapBuilder.addStatement("typeParam$it.fromMap(it as %T)", TypeUtil.mapStringAny())
                resolverParam.toMapBuilder.addStatement("typeParam$it.toMap(it)")
            }

        }

        if (name.asTypeElement()?.getAnnotation(Mapper::class.java) != null) {
            val mapperTypeName = ClassName.bestGuess("${name.name}Mapper")?.let {
                if (name.typeParams.isNotEmpty()) {
                    it.parameterizedBy(name.typeParams.mapNotNull { it.asFullTypeName() })
                } else {
                    it
                }
            }
            val fullTypeName = name.asFullTypeName()
            val helperInit = name.typeParams.map { "${buildHelperClazzName(it)}()" }.joinToString()
            resolverParam.fromMapBuilder.beginControlFlow("val myObj : %T = try", fullTypeName)
            resolverParam.fromMapBuilder.addStatement("obj.%N", accessorName)
            resolverParam.fromMapBuilder.endControlFlow()
            resolverParam.fromMapBuilder.beginControlFlow("catch(e: Exception)")
            if (name.hasEmptyConstructor()) {
                resolverParam.fromMapBuilder.addStatement("%T()", fullTypeName)
            } else {
                resolverParam.fromMapBuilder.addStatement("throw %T(%S)", Exception::class.java.asClassName(), "no empty ctr and not automatically filled")
            }

            resolverParam.fromMapBuilder.endControlFlow()
            resolverParam.fromMapBuilder.addStatement("%T($helperInit).fromMap(myObj, it as %T)", mapperTypeName!!, TypeUtil.mapStringAny())
            resolverParam.fromMapBuilder.addStatement("myObj")

            resolverParam.toMapBuilder.addStatement("%T($helperInit).toMap(it)", mapperTypeName!!)
            return
        }

        name.asTypeElement()?.apply {
            when {
                isAssignable(List::class.java) -> {
                    resolverParam.toMapBuilder.beginControlFlow("it.map")
                    resolverParam.fromMapBuilder.beginControlFlow("(it as? %T)?.map", TypeUtil.list(TypeUtil.any()))
                    resolveDeclaringName(name.typeParams[0], resolverParam, accessorName, typeParams)
                    resolverParam.toMapBuilder.endControlFlow()
                    resolverParam.fromMapBuilder.endControlFlow()
                }
                isAssignable(Map::class.java) -> {
                    resolverParam.toMapBuilder.beginControlFlow("it.map")
                    resolverParam.toMapBuilder.beginControlFlow("(it.key.let")
                    resolverParam.fromMapBuilder.beginControlFlow("(it as? %T)?.map", TypeUtil.mapAnyAny())
                    resolverParam.fromMapBuilder.beginControlFlow("(it.key.let")
                    resolveDeclaringName(name.typeParams[0], resolverParam, accessorName, typeParams)
                    resolverParam.toMapBuilder.endControlFlow()
                    resolverParam.toMapBuilder.beginControlFlow(" to it.value.let")
                    resolverParam.fromMapBuilder.endControlFlow()
                    resolverParam.fromMapBuilder.beginControlFlow(" to it.value.let")
                    resolveDeclaringName(name.typeParams[1], resolverParam, accessorName, typeParams)

                    resolverParam.toMapBuilder.endControlFlow()
                    resolverParam.toMapBuilder.addStatement(")")
                    resolverParam.toMapBuilder.endControlFlow()
                    resolverParam.toMapBuilder.addStatement("?.toMap()")
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
                        val fullTypeName = ProcessingContext.DeclaringName(this).asFullTypeName()
                        resolverParam.fromMapBuilder.addStatement("%T().fromMap(it as %T)", fullTypeName!!, TypeUtil.mapStringAny())

                        resolverParam.toMapBuilder.addStatement("%T().toMap(it)", fullTypeName!!)
                    }
                }
            }
        }
    }
}