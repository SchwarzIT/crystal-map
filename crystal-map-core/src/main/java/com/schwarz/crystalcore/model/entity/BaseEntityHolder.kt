package com.schwarz.crystalcore.model.entity

import com.schwarz.crystalcore.model.accessor.CblGenerateAccessorHolder
import com.schwarz.crystalcore.model.deprecated.DeprecatedModel
import com.schwarz.crystalcore.model.field.CblBaseFieldHolder
import com.schwarz.crystalcore.model.field.CblConstantHolder
import com.schwarz.crystalcore.model.field.CblFieldHolder
import com.schwarz.crystalcore.model.id.DocIdHolder
import com.schwarz.crystalcore.model.query.CblQueryHolder
import com.schwarz.crystalcore.model.source.IClassModel
import com.schwarz.crystalcore.model.source.ISourceModel
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName

abstract class BaseEntityHolder<T>(val sourceElement: ISourceModel<T>) :
    IClassModel<T> by sourceElement,
    ModelHolderWithFields {

    val fields: MutableMap<String, CblFieldHolder> = mutableMapOf()

    var isReduced: Boolean = false

    var abstractParts: Set<String> = mutableSetOf()

    val fieldConstants: MutableMap<String, CblConstantHolder> = mutableMapOf()

    val queries: MutableList<CblQueryHolder> = ArrayList()

    var comment: Array<String> = arrayOf()

    var generateAccessors: MutableList<CblGenerateAccessorHolder> = mutableListOf()

    var deprecated: DeprecatedModel? = null

    val basedOn: MutableList<BaseModelHolder<T>> = ArrayList()

    var docId: DocIdHolder? = null

    var reducesModels: List<ReducedModelHolder<T>> = emptyList()

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

    override fun hasFieldWithName(name: String): Boolean = fields.containsKey(name)
    override fun hasFieldConstantWithName(name: String): Boolean = fieldConstants.containsKey(name)

    fun collectAllSuperInterfaceNames(): List<TypeName> {
        val basedOnInterfaceTypeNames = basedOn.map { it.interfaceTypeName }
        val reducesModelsInterfaceTypeNames = reducesModels.map { ClassName(sourcePackage, "I${it.namePrefix}$sourceClazzSimpleName") }
        return listOf(*basedOnInterfaceTypeNames.toTypedArray(), *reducesModelsInterfaceTypeNames.toTypedArray())
    }

    fun collectAllSuperInterfaceFields(): List<ModelHolderWithFields> {
        val basedOnInterfaceTypes = basedOn
        val reducesModelsInterfaceTypes = reducesModels
        return listOf(*basedOnInterfaceTypes.toTypedArray(), *reducesModelsInterfaceTypes.toTypedArray())
    }
}
