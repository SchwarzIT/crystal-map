package com.kaufland.generation.model

import com.kaufland.generation.MapifyableImplGeneration
import com.kaufland.model.entity.EntityHolder
import com.kaufland.util.TypeUtil
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.jvm.throws
import kaufland.com.coachbasebinderapi.Entity
import kaufland.com.coachbasebinderapi.PersistenceConfig
import kaufland.com.coachbasebinderapi.PersistenceException
import java.util.*

class EntityGeneration {

    private val id: FunSpec
        get() = FunSpec.builder("getId").addModifiers(KModifier.PUBLIC).returns(TypeUtil.string().copy(nullable = true)).addStatement("return mDoc.get(%N) as %T", "_ID", TypeUtil.string().copy(nullable = true)).build()


    fun generateModel(holder: EntityHolder, useSuspend : Boolean): FileSpec {

        var companionSpec = TypeSpec.companionObjectBuilder()
        companionSpec.addProperty(idConstant())
        companionSpec.addFunctions(create(holder, useSuspend))
        companionSpec.addFunction(findById(holder, useSuspend))

        for (query in holder.queries) {
            query.queryFun(holder.dbName, holder, useSuspend)?.let {
                companionSpec.addFunction(it)
            }
        }

        for (generateAccessor in holder.generateAccessors) {
            generateAccessor.accessorFunSpec()?.let {
                companionSpec.addFunction(it)
            }
            generateAccessor.accessorPropertySpec()?.let {
                companionSpec.addProperty(it)
            }
        }

        val builderBuilder = BuilderClassGeneration.generateBaseBuilder(holder)

        val typeBuilder = TypeSpec.classBuilder(holder.entitySimpleName)
                .addModifiers(KModifier.PUBLIC)
                .addSuperinterface(TypeUtil.mapSupport())
                .addSuperinterface(holder.interfaceTypeName)
                .addFunction(EnsureTypesGeneration.ensureTypes(holder, false))
                .addFunction(CblDefaultGeneration.addDefaults(holder, false))
                .addFunction(CblConstantGeneration.addConstants(holder, false))
                .addProperty(PropertySpec.builder("mDoc", TypeUtil.mutableMapStringAny(), KModifier.PRIVATE).mutable().initializer("%T()", TypeUtil.hashMapStringAny()).build())
                .addProperty(PropertySpec.builder("mDocChanges", TypeUtil.mutableMapStringAnyNullable(), KModifier.PRIVATE).mutable().initializer("%T()", TypeUtil.hashMapStringAnyNullable()).build())
                .addFunction(contructor(holder))
                .addFunction(setAll(holder))
                .addFunctions(TypeConversionMethodsGeneration(useSuspend).generate())
                .addFunction(id).superclass(holder.sourceElement!!.asType().asTypeName())
                .addFunction(toMap(holder, useSuspend))
                .addFunction(BuilderClassGeneration.generateBuilderFun())

        holder.deprecated?.addDeprecated(typeBuilder)

        if(holder.entityType != Entity.Type.READONLY) {
            holder.docId?.let {
                companionSpec.addFunction(it.companionFunction(holder))
                typeBuilder.addFunction(it.buildExpectedDocId(holder))
                typeBuilder.addSuperinterface(TypeUtil.iDocId())
            }
        }
        if (holder.comment.isNotEmpty()) {
            typeBuilder.addKdoc(KDocGeneration.generate(holder.comment))
        }

        for (baseModelHolder in holder.basedOn) {
            typeBuilder.addSuperinterface(baseModelHolder.interfaceTypeName)
        }

        if(holder.modifierOpen){
            typeBuilder.addModifiers(KModifier.OPEN)
        }

        for (fieldHolder in holder.allFields) {

            fieldHolder.builderSetter(holder.dbName, holder.`package`, holder.entitySimpleName, true)?.let {
                builderBuilder.addFunction(it)
            }

            companionSpec.addProperties(fieldHolder.createFieldConstant())
            typeBuilder.addProperty(fieldHolder.property(holder.dbName, holder.abstractParts, true, holder.deprecated))
        }

        typeBuilder.addType(companionSpec.build())
        typeBuilder.addFunction(RebindMethodGeneration().generate(true))

        if(holder.entityType != Entity.Type.READONLY){
            typeBuilder.addFunction(delete(holder, useSuspend))
            typeBuilder.addFunction(save(holder, useSuspend))
        }

        typeBuilder.addType(builderBuilder.build())
        typeBuilder.addType(MapifyableImplGeneration.typeSpec(holder))
        typeBuilder.addAnnotation(MapifyableImplGeneration.impl(holder))

        return FileSpec.get(holder.`package`, typeBuilder.build())

    }

    private fun findById(holder: EntityHolder, useSuspend: Boolean): FunSpec {
       return FunSpec.builder("findById").addModifiers(evaluateModifiers(useSuspend)).addParameter("id", String::class).addAnnotation(JvmStatic::class)
               .addStatement("val result = %T.${getDocumentMethod(useSuspend)}(id, %S)", PersistenceConfig::class, holder.dbName)
               .addStatement("return if(result != null) %N(result) else null", holder.entitySimpleName)
               .returns(holder.entityTypeName.copy(true)).build()
    }


    private fun idConstant(): PropertySpec {
        return PropertySpec.builder("_ID", String::class, KModifier.PUBLIC, KModifier.FINAL).initializer("%S", "_id").addAnnotation(JvmField::class).build()
    }


    private fun setAll(holder: EntityHolder): FunSpec {

        val setAllBuilder = FunSpec.builder("setAll").addModifiers(KModifier.PUBLIC).addParameter("map", TypeUtil.mapStringAnyNullable()).addStatement("mDocChanges.putAll(map)", TypeUtil.mapStringAnyNullable(), PersistenceConfig::class, holder.dbName)

        return setAllBuilder.build()
    }

    private fun toMap(holder: EntityHolder, useSuspend: Boolean): FunSpec {

        var refreshDoc = "getId()?.let{%T.${getDocumentMethod(useSuspend)}(it, %S)} ?: mDoc"

        if(useSuspend){
            refreshDoc = "kotlinx.coroutines.runBlocking{$refreshDoc}"
        }

        val toMapBuilder = FunSpec.builder("toMap").addModifiers(KModifier.OVERRIDE).returns(TypeUtil.mutableMapStringAny()).addStatement("val doc = $refreshDoc", PersistenceConfig::class, holder.dbName)

        for (constantField in holder.fieldConstants.values) {
            toMapBuilder.addStatement("mDocChanges.put(%S, %N)", constantField.dbField, constantField.constantValueAccessorName)
        }

        toMapBuilder.addStatement("var temp = mutableMapOf<%T, %T>()", TypeUtil.string(), TypeUtil.any())
        toMapBuilder.addCode(CodeBlock.builder()
                .beginControlFlow("if(doc != null)")
                .addStatement("temp.putAll(doc)")
                .endControlFlow()
                .beginControlFlow("if(mDocChanges != null)")
                .beginControlFlow("mDocChanges.forEach")
                .beginControlFlow("if(it.value == null)").addStatement("temp.remove(it.key)").endControlFlow()
                .beginControlFlow("else").addStatement("temp[it.key] = it.value!!").endControlFlow()
                .endControlFlow()
                .endControlFlow()
                .addStatement("return temp").build())

        return toMapBuilder.build()
    }

    private fun delete(holder: EntityHolder, useSuspend: Boolean): FunSpec {
        return FunSpec.builder("delete").addModifiers(evaluateModifiers(useSuspend)).throws(PersistenceException::class).addStatement("getId()?.let{%T.${deleteDocumentMethod(useSuspend)}(it, %S)}", PersistenceConfig::class, holder.dbName).build()
    }

    private fun save(holder: EntityHolder, useSuspend: Boolean): FunSpec {
        val saveBuilder = FunSpec.builder("save").addModifiers(evaluateModifiers(useSuspend)).throws(PersistenceException::class).addStatement("val doc = toMap()")

        saveBuilder.addStatement("%T.${upsertDocumentMethod(useSuspend)}(doc, getId(), %S)", PersistenceConfig::class, holder.dbName)
        saveBuilder.addStatement("rebind(doc)")

        return saveBuilder.build()
    }

    private fun contructor(holder: EntityHolder): FunSpec {
        return FunSpec.constructorBuilder().addModifiers(KModifier.PUBLIC).addParameter("doc", TypeUtil.mapStringAny()).addStatement("rebind(ensureTypes(doc))").build()
    }

    private fun evaluateModifiers(useSuspend: Boolean): List<KModifier> {
       return if(useSuspend) listOf(KModifier.PUBLIC, KModifier.SUSPEND) else listOf(KModifier.PUBLIC)
    }

    private fun create(holder: EntityHolder, useSuspend: Boolean): List<FunSpec> {

        return Arrays.asList(
                FunSpec.builder("create").addModifiers(evaluateModifiers(useSuspend)).addParameter("id", String::class).addAnnotation(JvmStatic::class).addStatement("return %N(%T.${getDocumentMethod(useSuspend)}(id, %S) ?: mutableMapOf(_ID to id))",
                        holder.entitySimpleName, PersistenceConfig::class, holder.dbName).returns(holder.entityTypeName).build(),
                FunSpec.builder("create").addModifiers(evaluateModifiers(useSuspend)).addAnnotation(JvmStatic::class).addStatement("return %N(%T())",
                        holder.entitySimpleName, TypeUtil.hashMapStringAny()).returns(holder.entityTypeName).build(),
                FunSpec.builder("create").addModifiers(KModifier.PUBLIC).addParameter("map", TypeUtil.mutableMapStringAny()).addAnnotation(JvmStatic::class).addStatement("return %N(map)",
                        holder.entitySimpleName).returns(holder.entityTypeName).build()
        )
    }

    companion object {

        private fun getDocumentMethod(useSuspend: Boolean) : String{
            return "${if(useSuspend) "suspendingConnector" else "connector"}.getDocument"
        }

        private fun deleteDocumentMethod(useSuspend: Boolean) : String{
            return "${if (useSuspend) "suspendingConnector" else "connector"}.deleteDocument"
        }

        private fun upsertDocumentMethod(useSuspend: Boolean) : String{
            return "${if (useSuspend) "suspendingConnector" else "connector"}.upsertDocument"
        }
    }

}
