package com.schwarz.crystalksp.validation.mapper

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.schwarz.crystalcore.ILogger
import com.schwarz.crystalcore.model.source.IClassModel

object PreMapperValidation {
    @Throws(ClassNotFoundException::class)
    fun validate(
        mapperElement: IClassModel<KSNode>,
        logger: ILogger<KSNode>
    ) {
        val getterMap: MutableMap<String, KSFunctionDeclaration> = hashMapOf()
        val setterMap: MutableMap<String, KSFunctionDeclaration> = hashMapOf()

        (mapperElement.source as? KSClassDeclaration)?.declarations?.forEach { member ->
            if (member is KSFunctionDeclaration && member.annotations.any { it.shortName.asString() == "Mapify" }) {
                val simpleName = member.simpleName.asString()
                val isGetter = simpleName.startsWith("get")
                val isSetter = simpleName.startsWith("set")

                when {
                    isGetter -> getterMap[simpleName.substringAfter("get")] = member
                    isSetter -> setterMap[simpleName.substringAfter("set")] = member
                    else ->
                        logger.error(
                            "Mapify is only allowed on getters/setters/fields.",
                            member
                        )
                }
            }
        }

        val missingPairs =
            hashSetOf(
                *getterMap.keys.minus(setterMap.keys).toTypedArray(),
                *setterMap.keys.minus(getterMap.keys).toTypedArray()
            )

        missingPairs.forEach {
            val declaration = getterMap[it] ?: setterMap[it] ?: mapperElement.source
            logger.error(
                "Mapifyable needs to be applied on both getter and setter for property '$it'.",
                declaration
            )
        }
    }
}
