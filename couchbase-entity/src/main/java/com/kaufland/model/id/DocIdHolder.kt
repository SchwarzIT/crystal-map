package com.kaufland.model.id

import com.kaufland.generation.TypeConversionMethodsGeneration
import com.kaufland.model.deprecated.DeprecatedModel
import com.kaufland.model.entity.BaseEntityHolder
import com.kaufland.util.ConversionUtil
import com.kaufland.util.FieldExtractionUtil
import com.kaufland.util.TypeUtil
import com.squareup.kotlinpoet.*
import kaufland.com.coachbasebinderapi.DocId
import kaufland.com.coachbasebinderapi.Field
import org.apache.commons.lang3.text.WordUtils
import java.lang.StringBuilder
import javax.lang.model.type.TypeMirror

/**
 * Created by sbra0902 on 21.06.17.
 */

class DocIdHolder(docId: DocId) {

    val idPrefix = docId.prefix

    val concatedFields: List<String> = docId.fields.toList()


    fun companionFunction(entity: BaseEntityHolder): FunSpec {
        val spec = FunSpec.builder(COMPANION_BUILD_FUNCTION_NAME).returns(TypeUtil.string())
        val list = mutableListOf<String>()
        for (concatedField in concatedFields) {
            (entity.fields[concatedField] ?: entity.fieldConstants[concatedField])?.apply {
                spec.addParameter(accessorSuffix(), TypeUtil.parseMetaType(typeMirror, isIterable, null).copy(nullable = !isConstant))
                list.add(accessorSuffix())
            }
        }
        var statement = if (idPrefix.isNotBlank()) "$idPrefix:" else ""

        statement += list.joinToString(separator = ":", transform = { "$$it" })

        spec.addStatement("return %P", statement)
        return spec.build()
    }

    fun buildExpectedDocId(entity: BaseEntityHolder): FunSpec{
        val spec = FunSpec.builder(BUILD_FUNCTION_NAME).returns(TypeUtil.string()).addModifiers(KModifier.OVERRIDE)
        val list = mutableListOf<String>()
        for (concatedField in concatedFields) {
            (entity.fields[concatedField] ?: entity.fieldConstants[concatedField])?.apply {
                list.add(accessorSuffix())
            }
        }

        spec.addStatement("return $COMPANION_BUILD_FUNCTION_NAME(${list.joinToString(separator = ",")})")
        return spec.build()
    }

    companion object {
        const val BUILD_FUNCTION_NAME = "buildExpectedDocId"
        const val COMPANION_BUILD_FUNCTION_NAME = "buildDocId"
    }
}
