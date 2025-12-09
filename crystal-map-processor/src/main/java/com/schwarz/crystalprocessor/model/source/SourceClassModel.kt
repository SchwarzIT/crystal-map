package com.schwarz.crystalprocessor.model.source

import com.schwarz.crystalcore.javaToKotlinType
import com.schwarz.crystalcore.model.source.IClassModel
import com.schwarz.crystalcore.model.source.ISourceDeclaringName
import com.schwarz.crystalprocessor.ProcessingContext.asDeclaringName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import com.sun.tools.javac.code.Symbol
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier

class SourceClassModel(override val source: Element) : IClassModel<Element> {
    override val sourceClazzSimpleName: String = source.simpleName.toString()

    override val sourcePackage: String = if (source is Symbol.ClassSymbol) source.packge().toString() else ""

    override val sourceClazzTypeName: TypeName = ClassName(sourcePackage, sourceClazzSimpleName)
    override val typeName: TypeName = source.asType().asTypeName().javaToKotlinType()
    override val accessible: Boolean = source.modifiers.contains(Modifier.PUBLIC)

    override fun asDeclaringName(optinalIndexes: Array<Int>): ISourceDeclaringName {
        return source.asDeclaringName(optinalIndexes)
    }
}
