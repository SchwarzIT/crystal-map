package com.schwarz.crystalksp.model.source

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.Modifier
import com.schwarz.crystalcore.javaToKotlinType
import com.schwarz.crystalcore.model.source.IClassModel
import com.schwarz.crystalcore.model.source.ISourceDeclaringName
import com.schwarz.crystalcore.util.TypeUtil
import com.schwarz.crystalksp.ProcessingContext
import com.schwarz.crystalksp.ProcessingContext.resolveTypeNameWithProcessingTypes
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toClassName

class SourceClassModel(override val source: KSDeclaration) : IClassModel<KSNode> {
    override val sourceClazzSimpleName: String = source.simpleName.getShortName()
    override val sourcePackage: String = source.packageName.asString()

    override val sourceClazzTypeName: TypeName = ClassName(sourcePackage, sourceClazzSimpleName)
    override val typeName: TypeName =
        when (source) {
            is KSClassDeclaration -> source.toClassName().javaToKotlinType()
            is KSPropertyDeclaration -> {
                if (source.type.resolve().declaration is KSTypeParameter) {
                    TypeUtil.star()
                } else {
                    source.type.resolveTypeNameWithProcessingTypes().javaToKotlinType()
                }
            }
            else -> throw IllegalArgumentException("Unsupported type ${source::class.java.simpleName}")
        }
    override val accessible: Boolean = source.modifiers.contains(Modifier.PUBLIC)

    override fun asDeclaringName(optinalIndexes: Array<Int>): ISourceDeclaringName {
        return ProcessingContext.DeclaringName(source, 0, optinalIndexes)
    }
}
