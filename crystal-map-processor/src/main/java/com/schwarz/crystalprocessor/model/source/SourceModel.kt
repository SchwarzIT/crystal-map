package com.schwarz.crystalprocessor.model.source

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
import com.schwarz.crystalcore.model.source.ISourceDeprecated
import com.schwarz.crystalcore.model.source.ISourceField
import com.schwarz.crystalcore.model.source.ISourceModel
import com.schwarz.crystalcore.model.source.ISourceQuery
import com.schwarz.crystalcore.model.source.ISourceReduce
import com.schwarz.crystalcore.model.source.ISourceTypeConverterImporter
import com.schwarz.crystalcore.model.source.Parameter
import com.schwarz.crystalcore.model.source.SourceMemberField
import com.schwarz.crystalcore.model.source.SourceMemberFunction
import com.schwarz.crystalcore.javaToKotlinType
import com.schwarz.crystalcore.model.source.ISourceComment
import com.schwarz.crystalcore.model.source.ISourceDocId
import com.schwarz.crystalcore.model.source.ISourceEntity
import com.schwarz.crystalcore.model.source.ISourceMapWrapper
import com.schwarz.crystalcore.model.source.TypeConverterInterface
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import com.sun.tools.javac.code.Symbol
import org.apache.commons.lang3.text.WordUtils
import org.jetbrains.annotations.Nullable
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeMirror
import kotlin.coroutines.Continuation
import kotlin.metadata.KmClass
import kotlin.metadata.KmClassifier
import kotlin.metadata.KmType
import kotlin.metadata.KmTypeProjection
import kotlin.metadata.isNullable
import kotlin.metadata.jvm.KotlinClassMetadata

data class SourceModel(override val source: Element) :
    ISourceModel<Element>,
    IClassModel<Element> by SourceClassModel(source) {

    override val fullQualifiedName: String
        get() = source.toString()

    override val entityAnnotation: ISourceEntity? = source.getAnnotation(Entity::class.java)?.let { SourceEntity(it) }
    override val typeName: TypeName = source.asType().asTypeName()
    override val mapWrapperAnnotation: ISourceMapWrapper? =
        source.getAnnotation(MapWrapper::class.java)?.let { SourceMapWrapper(it) }
    override val commentAnnotation: ISourceComment? = source.getAnnotation(Comment::class.java)?.let { SourceComment(it) }
    override val deprecatedSource: ISourceDeprecated? = source.getAnnotation(Deprecated::class.java)?.let { SourceDeprecated(it) }
    override val docIdAnnotation: ISourceDocId? = source.getAnnotation(DocId::class.java)?.let { SourceDocId(it) }
    override val basedOnAnnotation: ISourceBasedOn? = source.getAnnotation(BasedOn::class.java)?.let { SourceBasedOn(it) }

    override val reduceAnnotations: List<ISourceReduce> =
        source.getAnnotation(Reduces::class.java)?.value?.toList()?.map { SourceReduce(it) } ?: emptyList()
    override val fieldAnnotations: List<ISourceField> =
        source.getAnnotation(Fields::class.java)?.value?.map { SourceField(it) }?.toList() ?: emptyList()

    override val queryAnnotations: List<ISourceQuery> =
        source.getAnnotation(Queries::class.java)?.value?.toList()?.map { SourceQuery(it) } ?: emptyList()
    override val typeConverterImporter: ISourceTypeConverterImporter? = source.getAnnotation(
        TypeConverterImporter::class.java
    )?.let { SourceTypeConverterImporter(it) }

    override val abstractParts: Set<String>

    private val typeConverterKmClass = ITypeConverter::class.java.getAnnotation(Metadata::class.java).toKmClass()

    override val typeConverterInterface: TypeConverterInterface? = source.getTypeConverterInterface()?.let {
        val (domainClassType, mapClassType) = it.arguments

        TypeConverterInterface(
            domainClassType.resolveToString().toClassName(),
            mapClassType.resolveToString().toClassName(),
            mapClassType.getGenericClassNames()
        )
    }

    private fun Element.getTypeConverterInterface(): KmType? {
        val kmClass = getAnnotation(Metadata::class.java)?.toKmClass()
        return kmClass?.supertypes?.find {
            val classifier = it.classifier
            classifier is KmClassifier.Class && typeConverterKmClass.name == classifier.name
        }
    }

    private fun Metadata.toKmClass(): KmClass = (KotlinClassMetadata.readStrict(this) as KotlinClassMetadata.Class).kmClass

    private fun String.toClassName(): ClassName = split('.').let {
        ClassName(it.subList(0, it.size - 1).joinToString("."), it.last())
    }

    private fun KmTypeProjection.resolveToString(): String {
        val classifier = type!!.classifier as KmClassifier.Class
        val typeName = classifier.name.replace('/', '.')
        return typeName
    }

    private fun KmTypeProjection.getGenericClassNames(): List<ClassNameDefinition> =
        type!!.arguments.fold(emptyList()) { classNameDefinitions, generic ->
            val type = generic.resolveToString().toClassName()
            classNameDefinitions + ClassNameDefinition(
                type.packageName,
                type.simpleName,
                generic.getGenericClassNames(),
                nullable = generic.type!!.isNullable
            )
        }

    override val isFinalModifier: Boolean = source.modifiers.contains(Modifier.FINAL)

    override fun firstNonParameterlessConstructor(): Element? {
        for (member in source.enclosedElements) {
            if (member.kind == ElementKind.CONSTRUCTOR) {
                val constructor = member as Symbol.MethodSymbol

                if (constructor.parameters.size != 0) {
                    return constructor
                }
            }
        }
        return null
    }

    override val isClassSource: Boolean = source.kind == ElementKind.CLASS

    override val isInterfaceSource: Boolean = source !is Symbol.ClassSymbol || !source.isInterface

    override val isPrivateModifier: Boolean = source.modifiers.contains(Modifier.PRIVATE)

    override fun logError(logger: ILogger<Element>, message: String) {
        logger.error(message, source)
    }

    override val relevantStaticFunctions: List<SourceMemberFunction>

    override val relevantStaticFields: List<SourceMemberField>

    init {
        abstractParts = findPossibleOverrides(source)
        val relevantStaticsFields = mutableListOf<SourceMemberField>()
        val relevantStaticsFunctions = mutableListOf<SourceMemberFunction>()
        parseStaticsFromStructure(source) {

            val accessor = it.getAnnotation(GenerateAccessor::class.java)?.let { SourceGenerateAccessor(it) }
            val docSegment = it.getAnnotation(DocIdSegment::class.java)?.let { SourceDocIdSegment(it) }

            if (accessor != null || docSegment != null) {

                when (it.kind) {
                    ElementKind.FIELD -> {
                        relevantStaticsFields.add(
                            SourceMemberField(
                                it.simpleName.toString(),
                                evaluateTypeName(
                                    it.asType(),
                                    it.getAnnotation(Nullable::class.java) != null
                                ),
                                docSegment,
                                accessor
                            )
                        )
                    }
                    ElementKind.METHOD -> {
                        (it as? ExecutableElement)?.let {
                            var isSuspend = false
                            val parameter = mutableListOf<Parameter>()
                            it.parameters.forEach {
                                if (isSuspendFunction(it)) {
                                    isSuspend = true
                                } else {
                                    parameter.add(
                                        Parameter(
                                            it.simpleName.toString(),
                                            evaluateTypeName(
                                                it.asType(),
                                                it.getAnnotation(Nullable::class.java) != null
                                            )
                                        )
                                    )
                                }
                            }

                            val returnType = it.returnType.asTypeName().javaToKotlinType().copy(it.getAnnotation(Nullable::class.java) != null)

                            relevantStaticsFunctions.add(
                                SourceMemberFunction(
                                    name = it.simpleName.toString(),
                                    isSuspend = isSuspend,
                                    parameters = parameter,
                                    generateAccessor = accessor,
                                    docIdSegment = docSegment,
                                    returnTypeName = returnType
                                )
                            )
                        }
                    }
                    else -> {}
                }
            }
        }
        relevantStaticFunctions = relevantStaticsFunctions
        relevantStaticFields = relevantStaticsFields
    }

    private fun isSuspendFunction(varElement: VariableElement): Boolean {
        return varElement.asType().toString().contains(Continuation::class.qualifiedName.toString())
    }

    private fun evaluateTypeName(typeMirror: TypeMirror, nullable: Boolean): TypeName {
        return typeMirror.asTypeName().javaToKotlinType().copy(nullable = nullable)
    }

    private fun parseStaticsFromStructure(cblEntityElement: Element, mapper: (Element) -> Unit) {
        for (childElement in cblEntityElement.enclosedElements) {
            if (childElement.modifiers.contains(Modifier.STATIC)) {
                if (childElement.kind == ElementKind.CLASS && childElement.simpleName.toString() == "Companion") {
                    for (companionMembers in childElement.enclosedElements) {
                        mapper.invoke(companionMembers)
                    }
                    continue
                }
                mapper.invoke(childElement)
            }
        }
    }

    private fun findPossibleOverrides(cblEntityElement: Element): HashSet<String> {
        var abstractSet = HashSet<String>()
        for (enclosedElement in cblEntityElement.enclosedElements) {
            if (enclosedElement.modifiers.contains(Modifier.ABSTRACT) && (enclosedElement.kind == ElementKind.FIELD || enclosedElement.kind == ElementKind.METHOD)) {
                var name = enclosedElement.simpleName.toString()
                if (name.startsWith("set")) {
                    abstractSet.add(WordUtils.uncapitalize(name.replace("set", "")))
                } else if (name.startsWith("get")) {
                    abstractSet.add(WordUtils.uncapitalize(name.replace("get", "")))
                } else {
                    abstractSet.add(name)
                }
            }
        }
        return abstractSet
    }
}
