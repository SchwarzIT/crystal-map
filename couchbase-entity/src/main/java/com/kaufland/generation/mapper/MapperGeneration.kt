package com.kaufland.generation.mapper

import com.kaufland.PostValidationException
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
                typeSpec.addProperty(
                    PropertySpec.builder("typeParam$index", TypeUtil.iMapifyable(TypeVariableName(typeVariableSymbol.name).copy(nullable = true)))
                        .initializer("typeParam$index")
                        .addModifiers(KModifier.PRIVATE)
                        .build()
                )
                constructorBuilder.addParameter("typeParam$index", TypeUtil.iMapifyable(TypeVariableName(typeVariableSymbol.name).copy(nullable = true)))
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
        for (fieldWithTypeParam in holder.fields.values.filter { it.typeHandleMode == MapifyHolder.TypeHandleMode.MAPPER && it.declaringName.typeParams.isNotEmpty() }) {

            for (mapifyHelper in fieldWithTypeParam.declaringName.typeParams) {

                val helperClazzName = buildHelperClazzName(mapifyHelper)

                if (addedHelpers.contains(helperClazzName)) {
                    continue
                }
                addedHelpers.add(helperClazzName)

                val param = ResolverParam()
                try {
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
                } catch (e: Exception) {
                    throw PostValidationException(e, *fieldWithTypeParam.elements)
                }

                typeSpec.addType(
                    MapifyableImplGeneration.typeSpec(
                        MapifyableImplGeneration.Config(
                            modifiers = arrayOf(KModifier.PRIVATE),
                            clazzName = helperClazzName,
                            typeParam = mapifyHelper.asFullTypeName()!!.copy(nullable = true),
                            fromMap = { it.addCode(param.fromMapBuilder.build()).build() },
                            toMap = { it.addCode(param.toMapBuilder.build()).build() }
                        )
                    )
                )
            }
        }

        for (field in holder.fields.values) {

            typeSpec.addProperties(field.reflectionProperties(holder.sourceClazzTypeName))

            typeSpec.addProperty(
                PropertySpec.builder(
                    field.accessorName,
                    field.declaringName.asFullTypeName()?.copy(nullable = true)
                        ?: field.typeName.copy(nullable = true),
                    KModifier.PRIVATE
                ).receiver(mapperTypeParam).mutable(true)
                    .getter(field.getterFunSpec())
                    .setter(field.setterFunSpec())
                    .build()
            )

            try {
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
            } catch (e: Exception) {
                throw PostValidationException(e, *field.mapifyElement.elements)
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

    @Throws(Exception::class)
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

            if (name.hasEmptyConstructor()) {
                resolverParam.fromMapBuilder.addStatement("val myObj = obj.%N ?: %T()", accessorName, fullTypeName)
            } else {
                resolverParam.fromMapBuilder.addStatement("val myObj = obj.%N", accessorName)
            }

            resolverParam.fromMapBuilder.beginControlFlow("myObj?.let")
            resolverParam.fromMapBuilder.addStatement("obj ->")
            resolverParam.fromMapBuilder.addStatement("%T($helperInit).fromMap(obj, it as %T)", mapperTypeName!!, TypeUtil.mapStringAny())
            resolverParam.fromMapBuilder.endControlFlow()
            resolverParam.fromMapBuilder.addStatement("myObj")

            resolverParam.toMapBuilder.addStatement("%T($helperInit).toMap(it)", mapperTypeName!!)
            return
        }

        name.asTypeElement()?.apply {
            when {
                getAnnotation(Mapifyable::class.java) != null -> {
                    FieldExtractionUtil.typeMirror(getAnnotation(Mapifyable::class.java))?.apply {
                        val fullTypeName = ProcessingContext.DeclaringName(this).asFullTypeName()
                        resolverParam.fromMapBuilder.addStatement("%T().fromMap(it as %T)", fullTypeName!!, TypeUtil.mapStringAny())

                        resolverParam.toMapBuilder.addStatement("%T().toMap(it)", fullTypeName!!)
                    }
                }
                isAssignable(List::class.java) -> {
                    resolverParam.toMapBuilder.beginControlFlow("it.map")
                    val mapFun = if (name.typeParams[0].isNullable()) "map" else "mapNotNull"
                    resolverParam.fromMapBuilder.beginControlFlow("(it as? %T)?.$mapFun", TypeUtil.list(TypeUtil.any()))
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
                    resolverParam.fromMapBuilder.addStatement("%T().fromMap(it as %T)", TypeUtil.serializableMapifyable(name.asFullTypeName()!!.copy(nullable = false)), TypeUtil.mapStringAny())
                    resolverParam.toMapBuilder.addStatement("%T().toMap(it)", TypeUtil.serializableMapifyable(name.asFullTypeName()!!.copy(nullable = false)))
                }
                else -> {
                    throw Exception("${name.name} is not ${Mapifyable::class.java.simpleName} or plain or any other parseable type.")
                }
            }
        }
    }
}
