package com.kaufland.model.entity

import com.kaufland.model.accessor.CblGenerateAccessorHolder
import com.kaufland.model.deprecated.DeprecatedModel
import com.kaufland.model.field.CblBaseFieldHolder
import com.kaufland.model.field.CblConstantHolder
import com.kaufland.model.field.CblFieldHolder
import com.kaufland.model.id.DocIdHolder
import com.kaufland.model.query.CblQueryHolder
import com.kaufland.model.source.IClassModel
import com.kaufland.model.source.ISourceModel
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName

abstract class BaseEntityHolder(val sourceElement: ISourceModel) : IClassModel by sourceElement {

    val fields: MutableMap<String, CblFieldHolder> = mutableMapOf()

    var isReduced: Boolean = false

    var abstractParts: Set<String> = mutableSetOf()

    val fieldConstants: MutableMap<String, CblConstantHolder> = mutableMapOf()

    val queries: MutableList<CblQueryHolder> = ArrayList()

    var comment: Array<String> = arrayOf()

    var generateAccessors: MutableList<CblGenerateAccessorHolder> = mutableListOf()

    var deprecated: DeprecatedModel? = null

    val basedOn: MutableList<BaseModelHolder> = ArrayList()

    var docId: DocIdHolder? = null

    var reducesModels: List<ReducedModelHolder> = emptyList()

    val allFields: List<CblBaseFieldHolder>
        get() {
            val allField = ArrayList<CblBaseFieldHolder>()
            allField.addAll(fields.values)
            allField.addAll(fieldConstants.values)
            return allField
        }

    val entityTypeName: TypeName
        get() = ClassName(sourcePackage, entitySimpleName)

    open val entitySimpleName: String
        get() = sourceClazzSimpleName + "Entity"

    val interfaceSimpleName: String
        get() = "I$sourceClazzSimpleName"

    val interfaceTypeName: TypeName
        get() = ClassName(sourcePackage, interfaceSimpleName)
}
