package com.kaufland.model.query

import com.kaufland.model.entity.BaseEntityHolder
import com.kaufland.util.TypeUtil
import com.squareup.kotlinpoet.*
import kaufland.com.coachbasebinderapi.PersistenceConfig
import kaufland.com.coachbasebinderapi.query.Query
import org.apache.commons.lang3.text.WordUtils

/**
 * Created by sbra0902 on 21.06.17.
 */

class CblQueryHolder(private val mQuery: Query) {

    val fields: Array<String>
        get() = mQuery.fields


    fun queryFun(dbName: String, entityHolder: BaseEntityHolder): FunSpec? {

        val builder = FunSpec.builder(queryFunName).addModifiers(KModifier.PUBLIC).addAnnotation(JvmStatic::class).addStatement("val queryParams = %T()", TypeUtil.hashMapStringObject()).returns(TypeUtil.list(entityHolder.entityTypeName))

        fields.forEach {
            entityHolder.fields[it]?.apply {
                builder.addParameter(dbField, fieldType)
                builder.addStatement("queryParams[%N] = %N", constantName, dbField)
            }
            entityHolder.fieldConstants[it]?.apply {
                builder.addStatement("queryParams[%N] = %N", constantName, constantValueAccessorName)
            }
        }

        builder.addStatement("return %T.$QUERY_DOCUMENT_METHOD(%S, queryParams).map { create(it) }", PersistenceConfig::class, dbName)



        return builder.build()
    }

    private val queryFunName: String = "findBy${fields.joinToString(separator = "And") { WordUtils.capitalize(it.replace("_", " ")).replace(" ", "") }}"

    companion object{
        private val QUERY_DOCUMENT_METHOD = "getInstance().getConnector().queryDoc"
    }
}
