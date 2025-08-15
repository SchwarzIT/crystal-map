package com.schwarz.crystalcore.model.query

import com.schwarz.crystalcore.model.entity.BaseEntityHolder
import com.schwarz.crystalcore.model.field.CblFieldHolder
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.jvm.throws
import com.schwarz.crystalapi.PersistenceConfig
import com.schwarz.crystalapi.PersistenceException
import com.schwarz.crystalcore.generation.model.CblReduceGeneration
import com.schwarz.crystalcore.model.source.ISourceQuery
import com.schwarz.crystalcore.model.typeconverter.TypeConverterHolderForEntityGeneration
import com.schwarz.crystalcore.util.TypeUtil
import com.squareup.kotlinpoet.TypeName
import org.apache.commons.lang3.text.WordUtils

/**
 * Created by sbra0902 on 21.06.17.
 */

class CblQueryHolder(private val mQuerySource: ISourceQuery) {

    val fields: Array<String>
        get() = mQuerySource.fields

    fun <T>queryFun(
        dbName: String,
        entityHolder: BaseEntityHolder<T>,
        useSuspend: Boolean,
        typeConvertersByConvertedClass: Map<TypeName, TypeConverterHolderForEntityGeneration>
    ): FunSpec {
        val builder = FunSpec.builder(queryFunName)
            .addModifiers(KModifier.PUBLIC)
            .addAnnotation(JvmStatic::class)
            .throws(PersistenceException::class)
            .returns(TypeUtil.list(entityHolder.entityTypeName))

        if (useSuspend) {
            builder.addModifiers(KModifier.SUSPEND)
        }

        if (entityHolder.deprecated?.addDeprecatedFunctions(fields, builder) == true) {
            builder.addStatement("throw %T()", UnsupportedOperationException::class)
        } else {
            builder.addStatement(
                "val queryParams = mutableMapOf<%T, %T>()",
                TypeUtil.string(),
                TypeUtil.any()
            )

            fields.forEach {
                entityHolder.fields[it]?.apply {
                    builder.addParameter(dbField, fieldType)

                    builder.addQueryParamComparisonStatement(this, dbField, typeConvertersByConvertedClass)
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
        }
        return builder.build()
    }

    private fun FunSpec.Builder.addQueryParamComparisonStatement(
        fieldHolder: CblFieldHolder,
        value: String,
        typeConvertersByConvertedClass: Map<TypeName, TypeConverterHolderForEntityGeneration>
    ) {
        if (fieldHolder.isNonConvertibleClass) {
            addStatement(
                "queryParams[%N] = %N",
                fieldHolder.constantName,
                value
            )
        } else {
            addStatement(
                "queryParams[%N] = %T.write(%N) ?:\nthrow PersistenceException(\"Invalid·type-conversion:·value·must·not·be·null\")",
                fieldHolder.constantName,
                typeConvertersByConvertedClass.get(fieldHolder.fieldType)!!.instanceClassTypeName,
                value

            )
        }
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
