package com.kaufland.generation.model

import com.kaufland.generation.MapifyableImplGeneration
import com.kaufland.model.entity.EntityHolder
import com.kaufland.model.id.DocIdHolder
import com.kaufland.util.TypeUtil
import com.kaufland.util.TypeUtil.string
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.jvm.throws
import kaufland.com.coachbasebinderapi.Entity
import kaufland.com.coachbasebinderapi.PersistenceConfig
import kaufland.com.coachbasebinderapi.PersistenceException

class EntityGeneration {

    private val id: FunSpec
        get() = FunSpec.builder("getId")
            .addModifiers(KModifier.PUBLIC, KModifier.OVERRIDE)
            .returns(string().copy(nullable = true))
            .addStatement(
                "return mDoc.get(%N) as %T",
                "_ID",
                string().copy(nullable = true)
            )
            .build()

    fun generateModel(holder: EntityHolder, useSuspend: Boolean): FileSpec {
        val companionSpec = TypeSpec.companionObjectBuilder()
        companionSpec.addProperty(idConstant())
        companionSpec.addFunctions(create(holder, useSuspend))
        companionSpec.addFunction(findById(holder, useSuspend))
        companionSpec.addFunction(findByIds(holder, useSuspend))
        companionSpec.addFunctions(TypeConversionMethodsGeneration(useSuspend).generate())

        for (query in holder.queries) {
            query.queryFun(holder.dbName, holder, useSuspend).let {
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
            .addSuperinterface(TypeUtil.iEntity())
            .addSuperinterface(holder.interfaceTypeName)
            .addProperty(holder.dbNameProperty())
            .addFunction(EnsureTypesGeneration.ensureTypes(holder, false))
            .addFunction(CblDefaultGeneration.addDefaults(holder, false))
            .addFunction(CblConstantGeneration.addConstants(holder, false))
            .addProperty(
                PropertySpec.builder(
                    "mDoc",
                    TypeUtil.mutableMapStringAny(),
                    KModifier.PRIVATE
                ).mutable().initializer("%T()", TypeUtil.hashMapStringAny()).build()
            )
            .addProperty(
                PropertySpec.builder(
                    "mDocChanges",
                    TypeUtil.mutableMapStringAnyNullable(),
                    KModifier.PRIVATE
                ).mutable().initializer("%T()", TypeUtil.hashMapStringAnyNullable()).build()
            )
            .addFunction(constructor(holder))
            .addFunction(SetAllMethodGeneration().generate(holder, true))
            .addFunction(id).superclass(holder.sourceElement.typeName)
            .addFunction(toMap(holder, useSuspend))
            .addFunction(BuilderClassGeneration.generateBuilderFun())

        holder.deprecated?.addDeprecated(typeBuilder)

        holder.docId?.let {
            companionSpec.addFunction(it.companionFunction(holder))
            typeBuilder.addFunction(it.buildExpectedDocId(holder))
            typeBuilder.addSuperinterface(TypeUtil.iDocId())
        }

        if (holder.comment.isNotEmpty()) {
            typeBuilder.addKdoc(KDocGeneration.generate(holder.comment))
        }

        if (holder.modifierOpen) {
            typeBuilder.addModifiers(KModifier.OPEN)
        }

        for (fieldHolder in holder.allFields) {
            fieldHolder.builderSetter(
                holder.dbName,
                holder.`package`,
                holder.entitySimpleName,
                true,
                holder.deprecated
            )?.let {
                builderBuilder.addFunction(it)
            }

            companionSpec.addProperties(fieldHolder.createFieldConstant())
            typeBuilder.addProperty(
                fieldHolder.property(
                    holder.dbName,
                    holder.abstractParts,
                    true,
                    holder.deprecated
                )
            )
        }

        typeBuilder.addType(companionSpec.build())
        typeBuilder.addFunction(RebindMethodGeneration().generate(true))

        if (holder.entityType != Entity.Type.READONLY) {
            typeBuilder.addFunction(delete(holder, useSuspend))
            typeBuilder.addFunction(save(holder, useSuspend))
        }

        typeBuilder.addType(builderBuilder.build())
        typeBuilder.addType(MapifyableImplGeneration.typeSpec(holder))
        typeBuilder.addAnnotation(MapifyableImplGeneration.impl(holder))

        return FileSpec.get(holder.`package`, typeBuilder.build())
    }

    private fun findById(holder: EntityHolder, useSuspend: Boolean): FunSpec {
        return FunSpec.builder("findById").addModifiers(evaluateModifiers(useSuspend))
            .addParameter("id", String::class).addAnnotation(JvmStatic::class)
            .addStatement(
                "val result = %T.${getDocumentMethod(useSuspend)}(id, %S)",
                PersistenceConfig::class,
                holder.dbName
            )
            .addStatement("return if(result != null) %N(result) else null", holder.entitySimpleName)
            .returns(holder.entityTypeName.copy(true)).build()
    }

    private fun findByIds(holder: EntityHolder, useSuspend: Boolean): FunSpec {
        return FunSpec.builder("findByIds").addModifiers(evaluateModifiers(useSuspend))
            .addParameter("ids", TypeUtil.list(string()))
            .addAnnotation(JvmStatic::class)
            .addStatement(
                "val result = %T.${getDocumentsMethod(useSuspend)}(ids, %S)",
                PersistenceConfig::class,
                holder.dbName
            )
            .addStatement(
                "return result.filterNotNull().mapNotNull { %N(it) }",
                holder.entitySimpleName
            )
            .returns(TypeUtil.list(holder.entityTypeName.copy(true))).build()
    }

    private fun idConstant(): PropertySpec {
        return PropertySpec.builder("_ID", String::class, KModifier.PUBLIC, KModifier.FINAL)
            .initializer("%S", "_id").addAnnotation(JvmField::class).build()
    }

    private fun toMap(holder: EntityHolder, useSuspend: Boolean): FunSpec {
        var refreshDoc = "getId()?.let{%T.${getDocumentMethod(useSuspend)}(it, %S)} ?: mDoc"

        if (useSuspend) {
            refreshDoc = "kotlinx.coroutines.runBlocking{$refreshDoc}"
        }

        val toMapBuilder = FunSpec.builder("toMap").addModifiers(KModifier.OVERRIDE)
            .returns(TypeUtil.mutableMapStringAny())
            .addStatement("val doc = $refreshDoc", PersistenceConfig::class, holder.dbName)

        for (constantField in holder.fieldConstants.values) {
            toMapBuilder.addStatement(
                "mDocChanges.put(%S, %N)",
                constantField.dbField,
                constantField.constantValueAccessorName
            )
        }

        toMapBuilder.addStatement(
            "var temp = mutableMapOf<%T, %T>()",
            string(),
            TypeUtil.any()
        )
        toMapBuilder.addCode(
            CodeBlock.builder()
                .beginControlFlow("if(doc != null)")
                .addStatement("temp.putAll(doc)")
                .endControlFlow()
                .beginControlFlow("if(mDocChanges != null)")
                .beginControlFlow("mDocChanges.forEach")
                .beginControlFlow("if(it.value == null)").addStatement("temp.remove(it.key)")
                .endControlFlow()
                .beginControlFlow("else").addStatement("temp[it.key] = it.value!!").endControlFlow()
                .endControlFlow()
                .endControlFlow()
                .addStatement("return temp").build()
        )

        return toMapBuilder.build()
    }

    private fun delete(holder: EntityHolder, useSuspend: Boolean): FunSpec {
        return FunSpec.builder("delete").addModifiers(evaluateModifiers(useSuspend))
            .throws(PersistenceException::class).addStatement(
                "getId()?.let{%T.${deleteDocumentMethod(useSuspend)}(it, %S)}",
                PersistenceConfig::class,
                holder.dbName
            ).build()
    }

    private fun save(holder: EntityHolder, useSuspend: Boolean): FunSpec {
        val saveBuilder = FunSpec.builder("save").addModifiers(evaluateModifiers(useSuspend))
            .throws(PersistenceException::class).addStatement("val doc = toMap()")

        var idResolve = "getId()"

        holder.docId?.let {
            idResolve += "?: ${DocIdHolder.BUILD_FUNCTION_NAME}()"
        }

        saveBuilder.addStatement("val docId = $idResolve")
        saveBuilder.addStatement(
            "val upsertedDoc = %T.${upsertDocumentMethod(useSuspend)}(doc, docId, %S)",
            PersistenceConfig::class,
            holder.dbName
        )
        saveBuilder.addStatement("rebind(upsertedDoc)")

        return saveBuilder.build()
    }

    private fun constructor(holder: EntityHolder): FunSpec {
        return FunSpec.constructorBuilder().addModifiers(KModifier.PUBLIC)
            .addParameter("doc", TypeUtil.mapStringAny()).addStatement("rebind(ensureTypes(doc))")
            .build()
    }

    private fun evaluateModifiers(useSuspend: Boolean): List<KModifier> {
        return if (useSuspend) listOf(
            KModifier.PUBLIC,
            KModifier.SUSPEND
        ) else listOf(KModifier.PUBLIC)
    }

    private fun create(holder: EntityHolder, useSuspend: Boolean): List<FunSpec> {

        return listOf(
            FunSpec.builder("create").addModifiers(evaluateModifiers(useSuspend))
                .addParameter("id", String::class).addAnnotation(JvmStatic::class).addStatement(
                    "return %N(%T.${getDocumentMethod(useSuspend)}(id, %S) ?: mutableMapOf(_ID to id))",
                    holder.entitySimpleName, PersistenceConfig::class, holder.dbName
                ).returns(holder.entityTypeName).build(),
            FunSpec.builder("create").addModifiers(evaluateModifiers(useSuspend))
                .addAnnotation(JvmStatic::class).addStatement(
                    "return %N(%T())",
                    holder.entitySimpleName, TypeUtil.hashMapStringAny()
                ).returns(holder.entityTypeName).build(),
            FunSpec.builder("create").addModifiers(KModifier.PUBLIC)
                .addParameter("map", TypeUtil.mutableMapStringAny()).addAnnotation(JvmStatic::class)
                .addStatement(
                    "return %N(map)",
                    holder.entitySimpleName
                ).returns(holder.entityTypeName).build()
        )
    }

    companion object {

        private fun getDocumentMethod(useSuspend: Boolean): String {
            return "${if (useSuspend) "suspendingConnector" else "connector"}.getDocument"
        }

        private fun getDocumentsMethod(useSuspend: Boolean): String {
            return "${if (useSuspend) "suspendingConnector" else "connector"}.getDocuments"
        }

        private fun deleteDocumentMethod(useSuspend: Boolean): String {
            return "${if (useSuspend) "suspendingConnector" else "connector"}.deleteDocument"
        }

        private fun upsertDocumentMethod(useSuspend: Boolean): String {
            return "${if (useSuspend) "suspendingConnector" else "connector"}.upsertDocument"
        }
    }
}
