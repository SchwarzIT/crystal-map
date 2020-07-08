package com.kaufland.generation

import com.kaufland.model.entity.EntityHolder
import com.kaufland.util.TypeUtil
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.jvm.throws
import kaufland.com.coachbasebinderapi.PersistenceConfig
import kaufland.com.coachbasebinderapi.PersistenceException
import java.util.*

class EntityGeneration {

    private val id: FunSpec
        get() = FunSpec.builder("getId").addModifiers(KModifier.PUBLIC).returns(String::class).addStatement("return mDoc.get(%N) as %T", "_ID", TypeUtil.string()).build()


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

        val typeBuilder = TypeSpec.classBuilder(holder.entitySimpleName).addModifiers(KModifier.PUBLIC).addSuperinterface(TypeUtil.mapSupport())
                .addFunction(CblDefaultGeneration.addDefaults(holder))
                .addFunction(CblConstantGeneration.addConstants(holder))
                .addProperty(PropertySpec.builder("mDoc", TypeUtil.mutableMapStringObject(), KModifier.PRIVATE).mutable().initializer("%T()", TypeUtil.hashMapStringObject()).build())
                .addProperty(PropertySpec.builder("mDocChanges", TypeUtil.mutableMapStringObject(), KModifier.PRIVATE).mutable().initializer("%T()", TypeUtil.hashMapStringObject()).build())
                .addFunction(contructor(holder)).addFunction(setAll(holder)).addFunctions(TypeConversionMethodsGeneration(useSuspend).generate()).addFunction(id).superclass(holder.sourceElement!!.asType().asTypeName())
                .addFunction(toMap(holder, useSuspend))
                .addFunction(BuilderClassGeneration.generateBuilderFun())

        for (fieldHolder in holder.allFields) {

            fieldHolder.builderSetter(holder.dbName, holder.`package`, holder.entitySimpleName, true)?.let {
                builderBuilder.addFunction(it)
            }

            companionSpec.addProperties(fieldHolder.createFieldConstant())
            typeBuilder.addProperty(fieldHolder.property(holder.dbName, holder.abstractParts, true))
        }

        typeBuilder.addType(companionSpec.build())
        typeBuilder.addFunction(RebindMethodGeneration().generate(true))
        typeBuilder.addFunction(delete(holder, useSuspend))
        typeBuilder.addFunction(save(holder, useSuspend))

        typeBuilder.addType(builderBuilder.build())

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

        val setAllBuilder = FunSpec.builder("setAll").addModifiers(KModifier.PUBLIC).addParameter("map", TypeUtil.mapStringObject()).addStatement("mDocChanges.putAll(map)", TypeUtil.mapStringObject(), PersistenceConfig::class, holder.dbName)

        return setAllBuilder.build()
    }

    private fun toMap(holder: EntityHolder, useSuspend: Boolean): FunSpec {

        var refreshDoc = "%T.${getDocumentMethod(useSuspend)}(getId(), %S)"

        if(useSuspend){
            refreshDoc = "kotlinx.coroutines.runBlocking{$refreshDoc}"
        }

        val toMapBuilder = FunSpec.builder("toMap").addModifiers(KModifier.OVERRIDE).returns(TypeUtil.mutableMapStringObject()).addStatement("val doc = $refreshDoc", PersistenceConfig::class, holder.dbName)

        for (constantField in holder.fieldConstants.values) {
            toMapBuilder.addStatement("mDocChanges.put(%S, %S)", constantField.dbField, constantField.constantValue)
        }

        toMapBuilder.addStatement("var temp = %T()", TypeUtil.hashMapStringObject())
        toMapBuilder.addCode(CodeBlock.builder().beginControlFlow("if(doc != null)").addStatement("temp.putAll(doc)").endControlFlow().beginControlFlow("if(mDocChanges != null)").addStatement("temp.putAll(mDocChanges)").endControlFlow().addStatement("return temp").build())

        return toMapBuilder.build()
    }

    private fun delete(holder: EntityHolder, useSuspend: Boolean): FunSpec {
        return FunSpec.builder("delete").addModifiers(evaluateModifiers(useSuspend)).throws(PersistenceException::class).addStatement("%T.${deleteDocumentMethod(useSuspend)}(getId(), %S)", PersistenceConfig::class, holder.dbName).build()
    }

    private fun save(holder: EntityHolder, useSuspend: Boolean): FunSpec {
        val saveBuilder = FunSpec.builder("save").addModifiers(evaluateModifiers(useSuspend)).throws(PersistenceException::class).addStatement("val doc = toMap()")

        saveBuilder.addStatement("%T.${upsertDocumentMethod(useSuspend)}(doc, getId(), %S)", PersistenceConfig::class, holder.dbName)
        saveBuilder.addStatement("rebind(doc)")

        return saveBuilder.build()
    }

    private fun contructor(holder: EntityHolder): FunSpec {
        return FunSpec.constructorBuilder().addModifiers(KModifier.PUBLIC).addParameter("doc", TypeUtil.mapStringObject()).addStatement("rebind(doc)").build()
    }

    private fun evaluateModifiers(useSuspend: Boolean): List<KModifier> {
       return if(useSuspend) listOf(KModifier.PUBLIC, KModifier.SUSPEND) else listOf(KModifier.PUBLIC)
    }

    private fun create(holder: EntityHolder, useSuspend: Boolean): List<FunSpec> {

        return Arrays.asList(
                FunSpec.builder("create").addModifiers(evaluateModifiers(useSuspend)).addParameter("id", String::class).addAnnotation(JvmStatic::class).addStatement("return %N(%T.${getDocumentMethod(useSuspend)}(id, %S))",
                        holder.entitySimpleName, PersistenceConfig::class, holder.dbName).returns(holder.entityTypeName).build(),
                FunSpec.builder("create").addModifiers(evaluateModifiers(useSuspend)).addAnnotation(JvmStatic::class).addStatement("return %N(%T.${getDocumentMethod(useSuspend)}(null, %S))",
                        holder.entitySimpleName, PersistenceConfig::class, holder.dbName).returns(holder.entityTypeName).build(),
                FunSpec.builder("create").addModifiers(KModifier.PUBLIC).addParameter("map", TypeUtil.mutableMapStringObject()).addAnnotation(JvmStatic::class).addStatement("return %N(map)",
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
