package com.schwarz.crystalksp

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.FunctionKind
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.Modifier
import com.schwarz.crystalapi.Entity
import com.schwarz.crystalapi.mapify.Mapifyable
import com.schwarz.crystalapi.query.Query
import com.schwarz.crystalcore.ILogger
import com.schwarz.crystalcore.javaToKotlinType
import com.schwarz.crystalcore.model.source.ISourceDeclaringName
import com.schwarz.crystalcore.util.TypeUtil
import com.schwarz.crystalksp.util.getArgument
import com.schwarz.crystalksp.model.source.SourceMapifyable
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.sun.tools.javac.code.Symbol
import com.sun.tools.javac.code.Type
import java.math.BigDecimal
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.type.PrimitiveType
import javax.lang.model.type.TypeMirror
import kotlin.metadata.KmType
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

    // KSP provides a Resolver and Logger directly to the SymbolProcessor.
    // These should not be late-init properties in an object, but rather passed in.
    // For this example, we'll keep the structure but you should pass these through your processor.
    lateinit var resolver: Resolver
    lateinit var logger: ILogger<KSNode>

    val processingTypes: HashMap<String, TypeName> = hashMapOf()

    val createdQualifiedClassNames: MutableSet<ClassName> = hashSetOf()

    /**
     * Converts a KSDeclaration to a DeclaringName with optional nullable indexes.
     */
    fun KSClassDeclaration.asDeclaringName(optionalIndexes: Array<Int>): DeclaringName {
        return DeclaringName(this.asStarProjectedType(), 0, optionalIndexes)
    }

    fun KSPropertyDeclaration.asDeclaringName(optionalIndexes: Array<Int>): DeclaringName {
        return DeclaringName(this.type!!.resolve(), 0, optionalIndexes)
    }

    fun KSTypeReference.resolveTypeNameWithProcessingTypes(): TypeName {
        return this.resolve().resolveTypeNameWithProcessingTypes()
    }

    fun KSType.resolveTypeNameWithProcessingTypes(): TypeName {
        return try {
            this.toTypeName()
        } catch (e: IllegalArgumentException) {
            hackyResolving(this.toString())
        } catch (e: NoSuchElementException) {
            if (this.declaration is KSTypeParameter) {
                // Its a TypeParam
                TypeVariableName(this.toString())

            } else {
                hackyResolving(this.toString())
            }
        }
    }

    private fun hackyResolving(stringValue: String): TypeName {
        // error types looks like this List<INVARIANT TaskEntity>
        val splitted = stringValue.replace('<', ' ').replace('>', ' ').split(" ")
        var isList = false
        for (item in splitted) {
            if (item == "INVARIANT" || item == "ERROR" || item == "TYPE:" || item.trim()
                    .isEmpty()
            ) {
                continue
            }
            if (item == "List") {
                isList = true
                continue
            }
            return if (isList) {
                System.out.println("value " + item)
                TypeUtil.list(processingTypes[item]!!)
            } else {
                processingTypes[item]!!
            }
        }
        throw IllegalArgumentException("unknown type $stringValue")
    }

    data class DeclaringName(
        private val ksType: KSType,
        private val relevantIndex: Int = 0,
        private val nullableIndexes: Array<Int> = emptyArray()
    ) : ISourceDeclaringName {
        val declaration: KSDeclaration = ksType.declaration
        val realTypeName = ksType.resolveTypeNameWithProcessingTypes()

        override val name: String = realTypeName.copy(nullable = false).toString().let {
            if (it.contains("<")) {
                it.substring(0, it.indexOf("<")).trim { it <= ' ' }
            }else{
                it
            }
        }
        override val typeParams: List<DeclaringName> =
            ksType.arguments.mapIndexedNotNull { index, typeArgument ->

                if (typeArgument.type == null) {
                    return@mapIndexedNotNull null
                }
                DeclaringName(
                    ksType = typeArgument.type!!.resolve(),
                    relevantIndex = relevantIndex + index + 1,
                    nullableIndexes = nullableIndexes
                )
            }

        override fun asTypeName(): TypeName? {
            val className =
                createdQualifiedClassNames.firstOrNull { it.canonicalName == realTypeName.toString() }
            if (className != null) {
                return className.copy(nullable = isNullable())
            }

            return when (declaration) {
                is KSTypeParameter -> TypeVariableName(declaration.name.asString()).copy(nullable = isNullable())
                is KSClassDeclaration -> declaration.toClassName().copy(nullable = isNullable())
                else -> {
                    logger.error(
                        "Unsupported declaration type for type name: ${declaration.qualifiedName}",
                        ksType.declaration
                    )
                    null
                }
            }
        }

        override fun asFullTypeName(): TypeName? = asTypeName()?.let {
            if (it is ClassName && typeParams.isNotEmpty()) {
                it.parameterizedBy(typeParams.mapNotNull { if (it.isTypeVar()) TypeVariableName(it.name) else it.asFullTypeName() }).also {
                    System.out.println("##" + it + typeParams.map { it.ksType })
                }
            } else {
                it
            }
        }

        override fun hasEmptyConstructor(): Boolean {
            if (declaration !is KSClassDeclaration) return false

            return declaration.declarations.any {
                it is KSFunctionDeclaration && it.functionKind == FunctionKind.MEMBER &&
                        it.simpleName.asString() == "<init>" &&
                        it.parameters.isEmpty() &&
                        it.modifiers.none { modifier -> modifier == Modifier.PRIVATE }
            }
        }

        override fun isPlainType(): Boolean {
            return plainTypeNames.contains(name)
        }

        override fun isTypeVar(): Boolean {
            return declaration is KSTypeParameter
        }

        override fun isNullable(): Boolean {
            return ksType.isMarkedNullable || nullableIndexes.contains(relevantIndex)
        }

        override fun isProcessingType(): Boolean {
            return createdQualifiedClassNames.any { it.canonicalName == name } &&
                    (name
                        .endsWith("Wrapper") || name
                        .endsWith("Entity"))
        }

        override fun isAssignable(clazz: KClass<*>): Boolean {
            val otherType =
                resolver.getClassDeclarationByName(clazz.qualifiedName!!)?.asStarProjectedType()
            //val thisBaseType = if(typeParams.isNotEmpty()) ksType.replace(listOf()) else ksType
            System.out.println(clazz.qualifiedName!!)
            System.out.println(otherType?.toClassName()?.canonicalName)
            System.out.println(
                ksType.toTypeName().toString() + otherType!!.isAssignableFrom(
                    ksType
                )
            )
            if(clazz.qualifiedName!! == "java.io.Serializable" && ksType.toTypeName().toString() == "com.schwarz.crystaldemo.mapper.DummyMapperSource.TestSerializable?") {
                System.out.println("##" + ksType.toTypeName().toString() + otherType!!.isAssignableFrom(
                    ksType
                ))

            }
            return otherType.let { otherType.isAssignableFrom(
                ksType.makeNotNullable()
            ) }
        }

        override fun <A : Annotation?, B> getAnnotationRepresent(annotationType: Class<A>?): B? {
            return when (annotationType) {
                Mapifyable::class.java -> getAnnotation(annotationType)?.let { SourceMapifyable(it) } as B?
                else -> throw NotImplementedError("Unsupported annotation type: ${annotationType?.canonicalName}")
            }
        }

        override fun <A : Annotation?> isAnnotationPresent(annotationType: Class<A>): Boolean {
            return getAnnotation(annotationType) != null
        }

        private fun <A : Annotation?> getAnnotation(annotationType: Class<A>): KSAnnotation? {
            return declaration.annotations.firstOrNull { annotation ->
                val annotationDeclaration = annotation.annotationType.resolve().declaration
                val annotationName =
                    (annotationDeclaration as? KSClassDeclaration)?.qualifiedName?.asString()
                annotationName == annotationType.name
            }
        }
    }
}