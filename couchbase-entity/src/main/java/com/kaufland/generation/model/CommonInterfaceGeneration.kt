package com.kaufland.generation.model

import com.kaufland.model.entity.BaseEntityHolder
import com.kaufland.util.TypeUtil
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec

class CommonInterfaceGeneration {

    fun generateModel(holder: BaseEntityHolder): FileSpec {

        var interfaceSpec = TypeSpec.interfaceBuilder(holder.interfaceSimpleName)
        interfaceSpec.addSuperinterface(TypeUtil.mapSupport())
        holder.basedOn.forEach { interfaceSpec.addSuperinterface(it.interfaceTypeName) }

        var companionSpec = TypeSpec.companionObjectBuilder()

        for (fieldHolder in holder.allFields) {
            val isBaseField = holder.basedOn.any {
                it.fields.containsKey(fieldHolder.dbField) || it.fieldConstants.containsKey(fieldHolder.dbField)
            }
            val propertySpec = fieldHolder.interfaceProperty(isBaseField)
            interfaceSpec.addProperty(propertySpec)

            companionSpec.addProperties(fieldHolder.createFieldConstant())
        }

        interfaceSpec.addType(companionSpec.build())

        return FileSpec.get(holder.`package`, interfaceSpec.build())
    }
}
