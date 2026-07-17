package com.schwarz.crystalcore.documentation

import com.schwarz.crystalcore.ILogger
import com.schwarz.crystalcore.model.entity.BaseEntityHolder
import com.schwarz.crystalcore.model.source.ISourceBasedOn
import com.schwarz.crystalcore.model.source.ISourceComment
import com.schwarz.crystalcore.model.source.ISourceDeclaringName
import com.schwarz.crystalcore.model.source.ISourceDeprecated
import com.schwarz.crystalcore.model.source.ISourceDocId
import com.schwarz.crystalcore.model.source.ISourceEntity
import com.schwarz.crystalcore.model.source.ISourceField
import com.schwarz.crystalcore.model.source.ISourceMapWrapper
import com.schwarz.crystalcore.model.source.ISourceModel
import com.schwarz.crystalcore.model.source.ISourceQuery
import com.schwarz.crystalcore.model.source.ISourceReduce
import com.schwarz.crystalcore.model.source.ISourceTypeConverterImporter
import com.schwarz.crystalcore.model.source.SourceMemberField
import com.schwarz.crystalcore.model.source.SourceMemberFunction
import com.schwarz.crystalcore.model.source.TypeConverterInterface
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName

/**
 * Minimal platform-free entity holder so the side-output generators can be
 * unit-tested without a kapt or KSP frontend.
 */
internal class TestEntityHolder(
    name: String,
) : BaseEntityHolder<Unit>(FakeSourceModel(name))

internal class FakeSourceModel(
    override val sourceClazzSimpleName: String,
) : ISourceModel<Unit> {
    override val entityAnnotation: ISourceEntity? = null
    override val fullQualifiedName: String = "test.$sourceClazzSimpleName"
    override val mapWrapperAnnotation: ISourceMapWrapper? = null
    override val commentAnnotation: ISourceComment? = null
    override val deprecatedSource: ISourceDeprecated? = null
    override val docIdAnnotation: ISourceDocId? = null
    override val basedOnAnnotation: ISourceBasedOn? = null
    override val relevantStaticFunctions: List<SourceMemberFunction> = emptyList()
    override val relevantStaticFields: List<SourceMemberField> = emptyList()
    override val reduceAnnotations: List<ISourceReduce> = emptyList()
    override val fieldAnnotations: List<ISourceField> = emptyList()
    override val queryAnnotations: List<ISourceQuery> = emptyList()
    override val typeConverterImporter: ISourceTypeConverterImporter? = null
    override val abstractParts: Set<String> = emptySet()
    override val typeConverterInterface: TypeConverterInterface? = null
    override val isPrivateModifier: Boolean = false
    override val isFinalModifier: Boolean = false
    override val isClassSource: Boolean = true
    override val isInterfaceSource: Boolean = false
    override val sourceClazzTypeName: TypeName = ClassName("test", sourceClazzSimpleName)
    override val sourcePackage: String = "test"
    override val typeName: TypeName = ClassName("test", sourceClazzSimpleName)
    override val source: Unit = Unit
    override val accessible: Boolean = true

    override fun logError(
        logger: ILogger<Unit>,
        message: String,
    ) = Unit

    override fun firstNonParameterlessConstructor(): Unit? = null

    override fun asDeclaringName(optinalIndexes: Array<Int>): ISourceDeclaringName = throw UnsupportedOperationException()
}
