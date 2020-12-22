package com.kaufland.generation

import com.kaufland.model.entity.BaseEntityHolder
import com.kaufland.model.entity.WrapperEntityHolder
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec

class CommonInterfaceGeneration{

    fun generateModel(holder: BaseEntityHolder): FileSpec {

        var interfaceSpec = TypeSpec.interfaceBuilder(holder.interfaceSimpleName)

        var companionSpec = TypeSpec.companionObjectBuilder()

        for (fieldHolder in holder.allFields) {
            val propertySpec = fieldHolder.interfaceProperty()
            interfaceSpec.addProperty(propertySpec)

            companionSpec.addProperties(fieldHolder.createFieldConstant())
        }

        interfaceSpec.addType(companionSpec.build())

        return FileSpec.get(holder.`package`, interfaceSpec.build())
    }

}