package com.schwarz.crystalprocessor.model.query

import com.schwarz.crystalprocessor.generation.model.CblReduceGeneration
import com.schwarz.crystalprocessor.generation.model.TypeConversionMethodsGeneration
import com.schwarz.crystalprocessor.model.entity.BaseEntityHolder
import com.schwarz.crystalprocessor.model.field.CblFieldHolder
import com.schwarz.crystalprocessor.util.TypeUtil
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.jvm.throws
import com.schwarz.crystalapi.PersistenceConfig
import com.schwarz.crystalapi.PersistenceException
import com.schwarz.crystalapi.query.Query
import org.apache.commons.lang3.text.WordUtils

/**
 * Created by sbra0902 on 21.06.17.
 */

class CblQueryHolder(private val mQuery: Query) {

    val fields: Array<String>
        get() = mQuery.fields

    fun queryFun(dbName: String, entityHolder: BaseEntityHolder, useSuspend: Boolean): FunSpec {
        val builder = FunSpec.builder(queryFunName)
            .addModifiers(KModifier.PUBLIC)
            .addAnnotation(JvmStatic::class)
            .throws(PersistenceException::class)
            .addStatement(
                "val queryParams = mutableMapOf<%T, %T>()",
                TypeUtil.string(),
                TypeUtil.any()
            )
            .returns(TypeUtil.list(entityHolder.entityTypeName))

        if (useSuspend) {
            builder.addModifiers(KModifier.SUSPEND)
        }

        fields.forEach {
            entityHolder.fields[it]?.apply {
                builder.addParameter(dbField, fieldType)

                builder.addQueryParamComparisonStatement(this, dbField)
            }
            entityHolder.fieldConstants[it]?.apply {
                builder.addStatement(
                    "queryParams[%N] = %N",
                    constantName,
                    constantValueAccessorName
                )
            }
        }

        builder.beginControlFlow(
            "return %T.${queryDocumentMethod(useSuspend)}(%S, queryParams, null, %N).map",
            PersistenceConfig::class,
            dbName,
            CblReduceGeneration.PROPERTY_ONLY_INCLUDES
        )
        builder.addStatement("%T(it)", entityHolder.entityTypeName)
        builder.endControlFlow()
        return builder.build()
    }

    private fun FunSpec.Builder.addQueryParamComparisonStatement(
        fieldHolder: CblFieldHolder,
        value: String
    ) {
        val classForTypeConversion = fieldHolder.evaluateClazzForTypeConversion()
        addStatement(
            "queryParams[%N] = ${TypeConversionMethodsGeneration.WRITE_METHOD_NAME}(%N, %N, %T::class) ?:\nthrow PersistenceException(\"Invalid·type-conversion:·value·must·not·be·null\")",
            fieldHolder.constantName,
            value,
            fieldHolder.constantName,
            classForTypeConversion
        )
    }

    private val queryFunName: String = "findBy${
    fields.joinToString(separator = "And") {
        WordUtils.capitalize(it.replace("_", " ")).replace(" ", "")
    }
    }"

    companion object {

        private fun queryDocumentMethod(useSuspend: Boolean): String {
            return "${if (useSuspend) "suspendingConnector" else "connector"}.queryDoc"
        }
    }
}
