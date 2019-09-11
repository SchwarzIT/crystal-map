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


    fun generateModel(holder: EntityHolder): FileSpec {

        var companionSpec = TypeSpec.companionObjectBuilder()
        companionSpec.addProperty(idConstant())
        companionSpec.addFunctions(create(holder))

        val typeBuilder = TypeSpec.classBuilder(holder.entitySimpleName).addModifiers(KModifier.PUBLIC).addSuperinterface(TypeUtil.mapSupport())
                .addFunction(CblDefaultGeneration.addDefaults(holder))
                .addFunction(CblConstantGeneration.addConstants(holder))
                .addProperty(PropertySpec.builder("mDoc", TypeUtil.mutableMapStringObject(), KModifier.PRIVATE).mutable().initializer("%T()", TypeUtil.hashMapStringObject()).build())
                .addProperty(PropertySpec.builder("mDocChanges", TypeUtil.mutableMapStringObject(), KModifier.PRIVATE).mutable().initializer("%T()", TypeUtil.hashMapStringObject()).build())
                .addFunction(contructor(holder)).addFunction(setAll(holder)).addFunctions(TypeConversionMethodsGeneration().generate()).addFunction(id).superclass(holder.sourceElement!!.asType().asTypeName())
                .addFunction(toMap(holder))

        for (fieldHolder in holder.allFields) {

            companionSpec.addProperties(fieldHolder.createFieldConstant())
            typeBuilder.addFunction(fieldHolder.getter(holder.dbName, true))

            val setter = fieldHolder.setter(holder.dbName, holder.entityTypeName, true)
            if (setter != null) {
                typeBuilder.addFunction(setter)
            }
        }

        typeBuilder.addType(companionSpec.build())
        typeBuilder.addFunction(RebindMethodGeneration().generate(true))
        typeBuilder.addFunction(delete(holder))
        typeBuilder.addFunction(save(holder))

        return FileSpec.get(holder.`package`, typeBuilder.build())

    }


    private fun idConstant(): PropertySpec {
        return PropertySpec.builder("_ID", String::class, KModifier.PUBLIC, KModifier.FINAL).initializer("%S", "_id").addAnnotation(JvmField::class).build()
    }


    private fun setAll(holder: EntityHolder): FunSpec {

        val setAllBuilder = FunSpec.builder("setAll").addModifiers(KModifier.PUBLIC).addParameter("map", TypeUtil.mapStringObject()).addStatement("mDocChanges.putAll(map)", TypeUtil.mapStringObject(), PersistenceConfig::class, holder.dbName)

        return setAllBuilder.build()
    }

    private fun toMap(holder: EntityHolder): FunSpec {

        val toMapBuilder = FunSpec.builder("toMap").addModifiers(KModifier.OVERRIDE).returns(TypeUtil.mutableMapStringObject()).addStatement("val doc = %T.$GET_DOCUMENT_METHOD(getId(), %S)", PersistenceConfig::class, holder.dbName)

        for (constantField in holder.fieldConstants) {
            toMapBuilder.addStatement("mDocChanges.put(%S, %S)", constantField.dbField, constantField.constantValue)
        }

        toMapBuilder.addStatement("var temp = %T()", TypeUtil.hashMapStringObject())
        toMapBuilder.addCode(CodeBlock.builder().beginControlFlow("if(doc != null)").addStatement("temp.putAll(doc)").endControlFlow().beginControlFlow("if(mDocChanges != null)").addStatement("temp.putAll(mDocChanges)").endControlFlow().addStatement("return temp").build())

        return toMapBuilder.build()
    }

    private fun delete(holder: EntityHolder): FunSpec {
        return FunSpec.builder("delete").addModifiers(KModifier.PUBLIC).throws(PersistenceException::class).addStatement("%T.$DELETE_DOCUMENT_METHOD(getId(), %S)", PersistenceConfig::class, holder.dbName).build()
    }

    private fun save(holder: EntityHolder): FunSpec {
        val saveBuilder = FunSpec.builder("save").addModifiers(KModifier.PUBLIC).throws(PersistenceException::class).addStatement("val doc = toMap()")

        saveBuilder.addStatement("%T.$UPSERT_DOCUMENT_METHOD(doc, getId(), %S)", PersistenceConfig::class, holder.dbName)
        saveBuilder.addStatement("rebind(doc)")

        return saveBuilder.build()
    }

    private fun contructor(holder: EntityHolder): FunSpec {
        return FunSpec.constructorBuilder().addModifiers(KModifier.PUBLIC).addParameter("doc", TypeUtil.mutableMapStringObject()).addStatement("rebind(doc)").build()
    }

    private fun create(holder: EntityHolder): List<FunSpec> {

        return Arrays.asList(
                FunSpec.builder("create").addModifiers(KModifier.PUBLIC).addParameter("id", String::class).addAnnotation(JvmStatic::class).addStatement("return %N(%T.$GET_DOCUMENT_METHOD(id, %S))",
                        holder.entitySimpleName, PersistenceConfig::class, holder.dbName).returns(holder.entityTypeName).build(),
                FunSpec.builder("create").addModifiers(KModifier.PUBLIC).addAnnotation(JvmStatic::class).addStatement("return %N(%T.$GET_DOCUMENT_METHOD(null, %S))",
                        holder.entitySimpleName, PersistenceConfig::class, holder.dbName).returns(holder.entityTypeName).build(),
                FunSpec.builder("create").addModifiers(KModifier.PUBLIC).addParameter("map", TypeUtil.mutableMapStringObject()).addAnnotation(JvmStatic::class).addStatement("return %N(map)",
                        holder.entitySimpleName).returns(holder.entityTypeName).build()
        )
    }

    companion object {

        private val GET_DOCUMENT_METHOD = "getInstance().getConnector().getDocument"

        private val DELETE_DOCUMENT_METHOD = "getInstance().getConnector().deleteDocument"

        private val UPSERT_DOCUMENT_METHOD = "getInstance().getConnector().upsertDocument"
    }

}
