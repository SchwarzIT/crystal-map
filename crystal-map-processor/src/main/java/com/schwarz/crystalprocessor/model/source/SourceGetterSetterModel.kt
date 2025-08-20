package com.schwarz.crystalprocessor.model.source

import com.schwarz.crystalcore.model.source.IClassModel
import com.schwarz.crystalcore.model.source.ISourceDeclaringName
import com.schwarz.crystalprocessor.ProcessingContext
import com.schwarz.crystalcore.javaToKotlinType
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import com.sun.tools.javac.code.Symbol
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier

class SourceGetterSetterModel(private val element: Symbol.MethodSymbol) : IClassModel<Element> {
    override val sourceClazzSimpleName: String = element.name.toString()
    override val sourceClazzTypeName: TypeName
        get() = throw NotImplementedError("$sourceClazzSimpleName -> sourceClazzTypeName")
    override val sourcePackage: String
        get() = throw NotImplementedError("$sourceClazzSimpleName -> sourcePackage")
    override val typeName: TypeName
        get() = element.params()[0]!!.asType().asTypeName().javaToKotlinType()

    override val source: Element = element
    override val accessible: Boolean = element.modifiers.contains(Modifier.PUBLIC)

    override fun asDeclaringName(optinalIndexes: Array<Int>): ISourceDeclaringName {
        return ProcessingContext.DeclaringName(element.params()[0]!!.asType(), 0, optinalIndexes)
    }
}
