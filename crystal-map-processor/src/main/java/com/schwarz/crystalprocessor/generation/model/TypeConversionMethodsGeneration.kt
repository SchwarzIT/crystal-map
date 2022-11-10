package com.schwarz.crystalprocessor.generation.model

import com.schwarz.crystalprocessor.util.TypeUtil
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeVariableName
import com.schwarz.crystalapi.PersistenceConfig
import com.schwarz.crystalapi.TypeConversionErrorWrapper

class TypeConversionMethodsGeneration(private val useSuspend: Boolean) {

    fun generate(): Collection<FunSpec> {
        return listOf(
            FunSpec.builder(READ_METHOD_NAME).addAnnotation(JvmStatic::class)
                .addParameter("value", TypeUtil.any().copy(nullable = true))
                .addParameter("fieldName", TypeUtil.string())
                .addParameter("clazz", TypeUtil.classStar())
                .addTypeVariable(TypeVariableName.invoke("reified T"))
                /** has to be inlined because the type inference would otherwise happen in the properties and cause there an classcastexception */
                .addModifiers(KModifier.INLINE).returns(TypeVariableName.invoke("T?"))
                /** use empty space otherwise its recognized as a single-expression*/
                .addCode(CodeBlock.of(" return ")).addCode(
                    CodeBlock.builder().beginControlFlow("try").addStatement(
                        "val conversion = %T.${getTypeConversionMethod(useSuspend)}[clazz] ?:\n return value as T?",
                        PersistenceConfig::class
                    ).addStatement("return conversion.read(value) as T?").endControlFlow()
                        .beginControlFlow("catch(ex: %T)", java.lang.Exception::class).addStatement(
                            "%T.${getConnector(useSuspend)}.invokeOnError(${TypeConversionErrorWrapper::class.qualifiedName}(ex, fieldName, value, clazz))",
                            PersistenceConfig::class
                        ).addStatement("null").endControlFlow().build()
                ).build(),

            FunSpec.builder(WRITE_METHOD_NAME).addAnnotation(JvmStatic::class)
                .addParameter("value", TypeUtil.any().copy(nullable = true))
                .addParameter("fieldName", TypeUtil.string())
                .addParameter("clazz", TypeUtil.classStar())
                .addTypeVariable(TypeVariableName.invoke("reified T"))
                /** has to be inlined because the type inference would otherwise happen in the properties and cause there an classcastexception */
                .addModifiers(KModifier.INLINE).returns(TypeVariableName.invoke("T?"))
                /** use empty space otherwise its recognized as a single-expression*/
                .addCode(CodeBlock.of(" return ")).addCode(
                    CodeBlock.builder().beginControlFlow("try").addStatement(
                        "val conversion = %T.${getTypeConversionMethod(useSuspend)}[clazz] ?:\n return value as T?",
                        PersistenceConfig::class
                    ).addStatement("return conversion.write(value) as T?").endControlFlow()
                        .beginControlFlow("catch(ex: %T)", java.lang.Exception::class).addStatement(
                            "%T.${getConnector(useSuspend)}.invokeOnError(${TypeConversionErrorWrapper::class.qualifiedName}(ex, fieldName, value, clazz))",
                            PersistenceConfig::class
                        ).addStatement("null").endControlFlow().build()
                ).build()
        )
    }

    companion object {
        const val READ_METHOD_NAME = "read"
        const val WRITE_METHOD_NAME = "write"

        private fun getTypeConversionMethod(useSuspend: Boolean): String {
            return "${getConnector(useSuspend)}.typeConversions"
        }

        private fun getConnector(useSuspend: Boolean): String {
            return if (useSuspend) "suspendingConnector" else "connector"
        }
    }
}
