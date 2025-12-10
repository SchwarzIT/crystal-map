package com.schwarz.crystalksp.model.source

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.isOpen
import com.google.devtools.ksp.isPrivate
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.Modifier
import com.schwarz.crystalapi.BasedOn
import com.schwarz.crystalapi.ClassNameDefinition
import com.schwarz.crystalapi.Comment
import com.schwarz.crystalapi.DocId
import com.schwarz.crystalapi.DocIdSegment
import com.schwarz.crystalapi.Entity
import com.schwarz.crystalapi.Fields
import com.schwarz.crystalapi.GenerateAccessor
import com.schwarz.crystalapi.ITypeConverter
import com.schwarz.crystalapi.MapWrapper
import com.schwarz.crystalapi.Reduces
import com.schwarz.crystalapi.TypeConverterImporter
import com.schwarz.crystalapi.deprecated.Deprecated
import com.schwarz.crystalapi.query.Queries
import com.schwarz.crystalcore.ILogger
import com.schwarz.crystalcore.model.source.IClassModel
import com.schwarz.crystalcore.model.source.ISourceBasedOn
import com.schwarz.crystalcore.model.source.ISourceComment
import com.schwarz.crystalcore.model.source.ISourceDeprecated
import com.schwarz.crystalcore.model.source.ISourceDocId
import com.schwarz.crystalcore.model.source.ISourceEntity
import com.schwarz.crystalcore.model.source.ISourceField
import com.schwarz.crystalcore.model.source.ISourceMapWrapper
import com.schwarz.crystalcore.model.source.ISourceModel
import com.schwarz.crystalcore.model.source.ISourceQuery
import com.schwarz.crystalcore.model.source.ISourceReduce
import com.schwarz.crystalcore.model.source.ISourceTypeConverterImporter
import com.schwarz.crystalcore.model.source.Parameter
import com.schwarz.crystalcore.model.source.SourceMemberField
import com.schwarz.crystalcore.model.source.SourceMemberFunction
import com.schwarz.crystalcore.model.source.TypeConverterInterface
import com.schwarz.crystalksp.ProcessingContext.resolveTypeNameWithProcessingTypes
import com.schwarz.crystalksp.util.getAnnotation
import com.schwarz.crystalksp.util.getArgument
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toClassName

data class SourceModel(override val source: KSClassDeclaration) :
    ISourceModel<KSNode>,
    IClassModel<KSNode> by SourceClassModel(source) {
    override val fullQualifiedName: String
        get() = source.qualifiedName?.asString() ?: ""

    override val entityAnnotation: ISourceEntity? = source.getAnnotation(Entity::class)?.let { SourceEntity(it) }
    override val typeName: TypeName = source.toClassName()
    override val mapWrapperAnnotation: ISourceMapWrapper? = source.getAnnotation(MapWrapper::class)?.let { SourceMapWrapper(it) }
    override val commentAnnotation: ISourceComment? = source.getAnnotation(Comment::class)?.let { SourceComment(it) }
    override val deprecatedSource: ISourceDeprecated? = source.getAnnotation(Deprecated::class)?.let { SourceDeprecated(it) }
    override val docIdAnnotation: ISourceDocId? = source.getAnnotation(DocId::class)?.let { SourceDocId(it) }
    override val basedOnAnnotation: ISourceBasedOn? = source.getAnnotation(BasedOn::class)?.let { SourceBasedOn(it) }
    override val reduceAnnotations: List<ISourceReduce> =
        source.getAnnotation(Reduces::class)?.getArgument<List<KSAnnotation>>("value")?.toList()?.map {
            SourceReduce(it)
        } ?: emptyList()
    override val fieldAnnotations: List<ISourceField> =
        source.getAnnotation(Fields::class)?.getArgument<List<KSAnnotation>>("value")?.map {
            SourceField(it)
        }?.toList() ?: emptyList()
    override val queryAnnotations: List<ISourceQuery> =
        source.getAnnotation(Queries::class)?.getArgument<List<KSAnnotation>>("value")?.toList()?.map {
            SourceQuery(it)
        } ?: emptyList()
    override val typeConverterImporter: ISourceTypeConverterImporter? =
        source.getAnnotation(TypeConverterImporter::class)?.let {
            SourceTypeConverterImporter(it)
        }

    override val abstractParts: Set<String>

    private val typeConverterClazzName = ITypeConverter::class.qualifiedName
    override val typeConverterInterface: TypeConverterInterface? =
        source.getAllSuperTypes().firstOrNull { it.declaration.qualifiedName?.asString() == typeConverterClazzName }?.let {
            val (domainClassType, mapClassType) = it.arguments
            TypeConverterInterface(
                domainClassType.resolveToString().toClassName(),
                mapClassType.resolveToString().toClassName(),
                mapClassType.getGenericClassNames()
            )
        }

    private fun String.toClassName(): ClassName =
        split('.').let {
            ClassName(it.subList(0, it.size - 1).joinToString("."), it.last())
        }

    private fun KSTypeArgument.resolveToString(): String {
        val typeName = this.type?.resolve()?.declaration?.qualifiedName?.asString()?.replace('/', '.') ?: ""
        return typeName
    }

    private fun KSTypeArgument.getGenericClassNames(): List<ClassNameDefinition> =
        type!!.resolve().arguments.fold(emptyList()) { classNameDefinitions, generic ->
            val type = generic.resolveToString().toClassName()
            classNameDefinitions +
                ClassNameDefinition(
                    type.packageName,
                    type.simpleName,
                    generic.getGenericClassNames(),
                    nullable = generic.type!!.resolve().isMarkedNullable
                )
        }

    override val isFinalModifier: Boolean = !source.isOpen()

    override fun firstNonParameterlessConstructor(): KSFunctionDeclaration? {
        return source.getConstructors().firstOrNull { it.parameters.isNotEmpty() }
    }

    override val isClassSource: Boolean = true
    override val isInterfaceSource: Boolean = false
    override val isPrivateModifier: Boolean = source.isPrivate()

    override fun logError(
        logger: ILogger<KSNode>,
        message: String
    ) {
        logger.error(message, source)
    }

    override val relevantStaticFunctions: List<SourceMemberFunction>
    override val relevantStaticFields: List<SourceMemberField>

    init {
        abstractParts = findPossibleOverrides(source)
        val relevantStaticsFields = mutableListOf<SourceMemberField>()
        val relevantStaticsFunctions = mutableListOf<SourceMemberFunction>()
        parseStaticsFromStructure(source) { declaration ->
            val accessor = declaration.getAnnotation(GenerateAccessor::class)?.let { SourceGenerateAccessor(it) }
            val docSegment = declaration.getAnnotation(DocIdSegment::class)?.let { SourceDocIdSegment(it) }

            if (accessor != null || docSegment != null) {
                when (declaration) {
                    is KSPropertyDeclaration -> {
                        relevantStaticsFields.add(
                            SourceMemberField(
                                declaration.simpleName.asString(),
                                declaration.type.resolveTypeNameWithProcessingTypes(),
                                docSegment,
                                accessor
                            )
                        )
                    }
                    is KSFunctionDeclaration -> {
                        var isSuspend = declaration.modifiers.contains(Modifier.SUSPEND)
                        val parameter = mutableListOf<Parameter>()
                        declaration.parameters.forEach {
                            val typeName = it.type.resolveTypeNameWithProcessingTypes()
                            if (typeName.toString().contains("kotlin.coroutines.Continuation")) {
                                isSuspend = true
                            } else {
                                parameter.add(
                                    Parameter(
                                        it.name?.asString() ?: "",
                                        typeName
                                    )
                                )
                            }
                        }

                        val returnType = declaration.returnType?.resolveTypeNameWithProcessingTypes()
                        if (returnType != null) {
                            relevantStaticsFunctions.add(
                                SourceMemberFunction(
                                    name = declaration.simpleName.asString(),
                                    isSuspend = isSuspend,
                                    parameters = parameter,
                                    generateAccessor = accessor,
                                    docIdSegment = docSegment,
                                    returnTypeName = returnType
                                )
                            )
                        }
                    }
                }
            }
        }
        relevantStaticFunctions = relevantStaticsFunctions
        relevantStaticFields = relevantStaticsFields
    }

    // KSP equivalent of the helper functions
    private fun parseStaticsFromStructure(
        cblEntityElement: KSClassDeclaration,
        mapper: (KSDeclaration) -> Unit
    ) {
        for (childElement in cblEntityElement.declarations) {
            if (childElement is KSClassDeclaration && childElement.isCompanionObject) {
                childElement.declarations.forEach { companionMember ->
                    mapper.invoke(companionMember)
                }
                continue
            }
            if (Modifier.JAVA_STATIC in childElement.modifiers) {
                mapper.invoke(childElement)
            }
        }
    }

    private fun findPossibleOverrides(cblEntityElement: KSClassDeclaration): HashSet<String> {
        val abstractSet = hashSetOf<String>()
        for (enclosedElement in cblEntityElement.declarations) {
            if (Modifier.ABSTRACT in enclosedElement.modifiers) {
                val name = enclosedElement.simpleName.asString()
                val propertyName =
                    when {
                        name.startsWith("set") -> name.replace("set", "").replaceFirstChar { it.lowercase() }
                        name.startsWith("get") -> name.replace("get", "").replaceFirstChar { it.lowercase() }
                        else -> name
                    }
                abstractSet.add(propertyName)
            }
        }
        return abstractSet
    }
}
