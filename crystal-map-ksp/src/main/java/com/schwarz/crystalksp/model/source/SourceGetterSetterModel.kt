package com.schwarz.crystalksp.model.source

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.Modifier
import com.schwarz.crystalcore.model.source.IClassModel
import com.schwarz.crystalcore.model.source.ISourceDeclaringName
import com.schwarz.crystalcore.javaToKotlinType
import com.schwarz.crystalksp.ProcessingContext
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toTypeName

class SourceGetterSetterModel(private val element: KSFunctionDeclaration) : IClassModel<KSNode> {
    override val sourceClazzSimpleName: String = element.qualifiedName?.asString() ?: ""
    override val sourceClazzTypeName: TypeName
        get() = throw NotImplementedError("$sourceClazzSimpleName -> sourceClazzTypeName")
    override val sourcePackage: String
        get() = throw NotImplementedError("$sourceClazzSimpleName -> sourcePackage")
    override val typeName: TypeName
        get() = element.parameters[0]!!.type.toTypeName().javaToKotlinType()

    override val source: KSNode = element
    override val accessible: Boolean = element.modifiers.contains(Modifier.PUBLIC)

    override fun asDeclaringName(optinalIndexes: Array<Int>): ISourceDeclaringName {
        return ProcessingContext.DeclaringName(element.parameters[0], 0, optinalIndexes)
    }
}
