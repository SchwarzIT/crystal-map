package com.kaufland.model.query

import com.kaufland.generation.model.TypeConversionMethodsGeneration
import com.kaufland.model.entity.BaseEntityHolder
import com.kaufland.model.field.CblFieldHolder
import com.kaufland.util.TypeUtil
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.jvm.throws
import kaufland.com.coachbasebinderapi.PersistenceConfig
import kaufland.com.coachbasebinderapi.PersistenceException
import kaufland.com.coachbasebinderapi.query.Query
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
            .addStatement("val queryParams = mutableMapOf<%T, %T>()", TypeUtil.string(), TypeUtil.any())
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
                builder.addStatement("queryParams[%N] = %N", constantName, constantValueAccessorName)
            }
        }

        builder.addStatement("return %T.${queryDocumentMethod(useSuspend)}(%S, queryParams).map { %T(it) }", PersistenceConfig::class, dbName, entityHolder.entityTypeName)

        return builder.build()
    }

    private fun FunSpec.Builder.addQueryParamComparisonStatement(fieldHolder: CblFieldHolder, value: String) {
        val classForTypeConversion = fieldHolder.evaluateClazzForTypeConversion()
        addStatement(
            "queryParams[%N] = ${TypeConversionMethodsGeneration.WRITE_METHOD_NAME}(%N, %T::class) ?:\nthrow PersistenceException(\"Invalid·type-conversion:·value·must·not·be·null\")",
            fieldHolder.constantName,
            value,
            classForTypeConversion
        )
    }

    private val queryFunName: String = "findBy${fields.joinToString(separator = "And") { WordUtils.capitalize(it.replace("_", " ")).replace(" ", "") }}"

    companion object {

        private fun queryDocumentMethod(useSuspend: Boolean): String {
            return "${if (useSuspend) "suspendingConnector" else "connector"}.queryDoc"
        }
    }
}
