package com.schwarz.crystalksp

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.FunctionKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Modifier
import com.schwarz.crystalapi.mapify.Mapifyable
import com.schwarz.crystalcore.ILogger
import com.schwarz.crystalcore.model.source.ISourceDeclaringName
import com.schwarz.crystalksp.model.source.SourceMapifyable
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeVariableName
import java.math.BigDecimal
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

private val plainTypeNames = setOf(
    String::class.qualifiedName,
    Int::class.qualifiedName,
    "java.lang.Integer",
    "java.lang.Double",
    "java.lang.Boolean",
    Double::class.qualifiedName,
    Long::class.qualifiedName,
    BigDecimal::class.qualifiedName,
    Boolean::class.qualifiedName,
)

object ProcessingContext {
    lateinit var resolver: Resolver
    lateinit var logger: ILogger<KSNode>

    val processingTypes: MutableMap<String, TypeName> = ConcurrentHashMap()

    val createdQualifiedClassNames: MutableSet<ClassName> = ConcurrentHashMap.newKeySet()

    val createdQualifiedClassNamesByCanonical: MutableMap<String, ClassName> = ConcurrentHashMap()

    fun cleanup() {
        processingTypes.clear()
        createdQualifiedClassNames.clear()
        createdQualifiedClassNamesByCanonical.clear()
    }

    fun KSTypeReference.resolveTypeNameWithProcessingTypes(): TypeName = this.resolve().resolveTypeNameWithProcessingTypes()

    fun KSType.resolveTypeNameWithProcessingTypes(): TypeName = try {
        this.toTypeName()
    } catch (e: IllegalArgumentException) {
        resolveFromProcessingTypes(this)
    } catch (e: NoSuchElementException) {
        if (this.declaration is KSTypeParameter) {
            (this.declaration as KSTypeParameter).toNullableSafeTypeVariableName()
        } else {
            resolveFromProcessingTypes(this)
        }
    }

    private fun KSTypeParameter.toNullableSafeTypeVariableName(): TypeName {
        val nullable = this.name.asString().endsWith("?")
        return if (nullable) {
            TypeVariableName(this.name.asString().removeSuffix("?")).copy(nullable)
        } else {
            TypeVariableName(this.name.asString())
        }
    }

    /**
     * Resolves a KSType that failed normal resolution by looking up the type name
     * in the processingTypes registry. This handles references to generated types
     * (e.g., EntityBEntity, SomeWrapper) that don't exist as source files.
     *
     * For parameterized types (e.g., List<EntityBEntity>), the outer type is resolved
     * normally and the type arguments are resolved recursively.
     */
    private val errorTypePattern = Regex("""<ERROR TYPE:\s*(\w+)>""")

    /**
     * Extracts the actual type name from a KSType declaration, handling KSP error type formats.
     * Error types may have simpleName like "<ERROR TYPE: TaskEntity>" or just "TaskEntity".
     */
    private fun extractTypeName(declaration: KSDeclaration): String {
        val raw = declaration.simpleName.asString()
        return errorTypePattern.find(raw)?.groupValues?.get(1) ?: raw
    }

    private fun resolveFromProcessingTypes(type: KSType): TypeName {
        val declaration = type.declaration
        val typeName = extractTypeName(declaration)

        // Direct lookup: the type itself is a processing type (e.g., EntityBEntity)
        processingTypes[typeName]?.let { resolved ->
            return if (type.arguments.isNotEmpty() && resolved is ClassName) {
                resolved.parameterizedBy(
                    type.arguments.map { arg ->
                        arg.type?.resolve()?.resolveTypeNameWithProcessingTypes()
                            ?: TypeVariableName("*")
                    },
                )
            } else {
                resolved
            }
        }

        // The outer type resolves but has error type arguments (e.g., List<EntityBEntity>)
        if (type.arguments.isNotEmpty()) {
            val outerType = try {
                (declaration as? KSClassDeclaration)?.toClassName()
            } catch (_: Exception) {
                null
            }

            if (outerType != null) {
                val resolvedArgs = type.arguments.map { arg ->
                    arg.type?.resolve()?.resolveTypeNameWithProcessingTypes()
                        ?: TypeVariableName("*")
                }
                return outerType.parameterizedBy(resolvedArgs)
            }
        }

        throw IllegalArgumentException("Cannot resolve type: $type (typeName=$typeName)")
    }

//    private fun KSClassDeclaration.toKSTypeRecursive() : KSType{
//        val typeArguments = typeParameters.map { typeParameter ->
//            resolver.getTypeArgument(resolver.createKSTypeReferenceFromKSType(typeParameter.bounds.first().resolve()), Variance.INVARIANT)
//        }
//        return asType(typeArguments)
//    }

    private fun KSClassDeclaration.toKSTypeRecursive(): KSType {
        val typeArguments = typeParameters.map { typeParameter ->
            val resolvedType = typeParameter.bounds.first().resolve()

            // Recursively handle nested type parameters
            val resolvedKSType = resolvedType.declaration.let { declaration ->
                if (declaration is KSClassDeclaration && declaration.typeParameters.isNotEmpty()) {
                    declaration.toKSTypeRecursive()
                } else {
                    resolvedType
                }
            }

            // Use the correct method to create a KSTypeArgument
            resolver.getTypeArgument(
                resolver.createKSTypeReferenceFromKSType(resolvedKSType),
                typeParameter.variance,
            )
        }
        return asType(typeArguments)
    }

    data class DeclaringName(
        private val declaration: KSAnnotated,
        private val relevantIndex: Int = 0,
        private val nullableIndexes: Array<Int> = emptyArray(),
    ) : ISourceDeclaringName {
        constructor(
            declaration: KSValueParameter,
            relevantIndex: Int,
            nullableIndexes: Array<Int>,
        ) : this(
            resolver.getClassDeclarationByName(declaration.name!!)!!,
            relevantIndex,
            nullableIndexes,
        )

        val ksType = when (declaration) {
            is KSPropertyDeclaration -> declaration.type!!.resolve()
            is KSClassDeclaration -> declaration.toKSTypeRecursive()
            is KSTypeParameter -> null // dont resolve KSTypeParameter since it will be resolved to any
            is KSTypeReference -> declaration.resolve()
            else -> throw IllegalArgumentException(
                "Unsupported type ${declaration::class.java.simpleName}",
            )
        }

        val realTypeName = ksType?.resolveTypeNameWithProcessingTypes() ?: (declaration as KSTypeParameter).toNullableSafeTypeVariableName()

        override val name: String = realTypeName.copy(nullable = false).toString().let {
            if (it.contains("<")) {
                it.substring(0, it.indexOf("<")).trim { it <= ' ' }
            } else {
                it
            }
        }
        override val typeParams: List<DeclaringName> = when (declaration) {
            is KSPropertyDeclaration -> declaration.type.resolve().arguments.mapIndexedNotNull { index, typeArgument ->
                DeclaringName(
                    declaration = typeArgument.type!!,
                    relevantIndex = relevantIndex + index + 1,
                    nullableIndexes = nullableIndexes,
                )
            }

            is KSDeclaration -> declaration.typeParameters.mapIndexedNotNull { index, typeArgument ->
                DeclaringName(
                    declaration = typeArgument,
                    relevantIndex = relevantIndex + index + 1,
                    nullableIndexes = nullableIndexes,
                )
            }

            is KSTypeReference -> declaration.resolve().arguments.mapIndexedNotNull { index, typeArgument ->
                DeclaringName(
                    declaration = typeArgument.type!!,
                    relevantIndex = relevantIndex + index + 1,
                    nullableIndexes = nullableIndexes,
                )
            }

            else -> throw IllegalArgumentException(
                "Unsupported type ${declaration::class.java.simpleName}",
            )
        }

        override fun asTypeName(): TypeName? {
            val className = createdQualifiedClassNamesByCanonical[realTypeName.toString()]
            if (className != null) {
                return className.copy(nullable = isNullable())
            }

            return when (declaration) {
                is KSTypeParameter -> declaration.toTypeVariableName().copy(nullable = isNullable())
                is KSClassDeclaration -> declaration.toClassName().copy(nullable = isNullable())
                is KSPropertyDeclaration -> declaration.type.resolveTypeNameWithProcessingTypes().copy(
                    isNullable(),
                )

                is KSTypeReference -> declaration.resolveTypeNameWithProcessingTypes().copy(
                    isNullable(),
                )

                else -> {
                    logger.error(
                        "Unsupported declaration type for type name: ${declaration::class.qualifiedName}",
                        ksType!!.declaration,
                    )
                    null
                }
            }
        }

        override fun asFullTypeName(): TypeName? = asTypeName()?.let {
            if (it is ClassName && typeParams.isNotEmpty()) {
                it.parameterizedBy(
                    typeParams.mapNotNull {
                        if (it.isTypeVar()) TypeVariableName(it.name) else it.asFullTypeName()
                    },
                )
            } else {
                it
            }
        }

        override fun hasEmptyConstructor(): Boolean {
            if (declaration !is KSClassDeclaration) return false

            return declaration.declarations.any {
                it is KSFunctionDeclaration &&
                    it.functionKind == FunctionKind.MEMBER &&
                    it.simpleName.asString() == "<init>" &&
                    it.parameters.isEmpty() &&
                    it.modifiers.none { modifier -> modifier == Modifier.PRIVATE }
            }
        }

        override fun isPlainType(): Boolean = plainTypeNames.contains(name)

        override fun isTypeVar(): Boolean =
            declaration is KSTypeParameter || (declaration is KSPropertyDeclaration && declaration.type.resolve().declaration is KSTypeParameter)

        override fun isNullable(): Boolean = realTypeName.isNullable || nullableIndexes.contains(relevantIndex)

        override fun isProcessingType(): Boolean =
            createdQualifiedClassNamesByCanonical.containsKey(name) && (name.endsWith("Wrapper") || name.endsWith("Entity"))

        override fun isAssignable(clazz: KClass<*>): Boolean {
            val otherType = resolver.getClassDeclarationByName(clazz.qualifiedName!!)?.asStarProjectedType()

            return if (ksType != null) {
                return otherType.let {
                    otherType?.isAssignableFrom(
                        ksType.makeNotNullable(),
                    ) == true
                }
            } else {
                false
            }
        }

        override fun <A : Annotation?, B> getAnnotationRepresent(annotationType: Class<A>?): B? = when (annotationType) {
            Mapifyable::class.java -> getAnnotation(
                annotationType,
            )?.let { SourceMapifyable(it) } as B?

            else -> throw NotImplementedError(
                "Unsupported annotation type: ${annotationType?.canonicalName}",
            )
        }

        override fun <A : Annotation?> isAnnotationPresent(annotationType: Class<A>): Boolean = getAnnotation(annotationType) != null

        private fun <A : Annotation?> getAnnotation(annotationType: Class<A>): KSAnnotation? =
            ksType?.declaration?.annotations?.firstOrNull { annotation ->
                val annotationDeclaration = annotation.annotationType.resolve().declaration
                val annotationName = (annotationDeclaration as? KSClassDeclaration)?.qualifiedName?.asString()
                annotationName == annotationType.name
            }
    }
}
