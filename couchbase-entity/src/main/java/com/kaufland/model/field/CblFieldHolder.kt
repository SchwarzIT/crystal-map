package com.kaufland.model.field

import com.kaufland.generation.TypeConversionMethodsGeneration
import com.kaufland.util.TypeUtil
import com.squareup.kotlinpoet.*
import kaufland.com.coachbasebinderapi.Field
import org.apache.commons.lang3.StringUtils

class CblFieldHolder(field: Field, allWrappers: List<String>) : CblBaseFieldHolder(field.name, field) {

    private var subEntityPackage: String? = null

    var subEntitySimpleName: String? = null
        private set

    var isSubEntityIsTypeParam: Boolean = false
        private set

    override var isIterable: Boolean = false

    val subEntityTypeName: TypeName
        get() = ClassName(subEntityPackage!!, subEntitySimpleName!!)


    val isTypeOfSubEntity: Boolean
        get() = !StringUtils.isBlank(subEntitySimpleName)

    init {
        if (allWrappers.contains(typeMirror.toString())) {

            subEntitySimpleName = TypeUtil.getSimpleName(typeMirror!!) + "Wrapper"
            subEntityPackage = TypeUtil.getPackage(typeMirror)
            isSubEntityIsTypeParam = field.list

        }
        if (field.list) {
            isIterable = true
        }
    }

    override fun getter(dbName: String?, useMDocChanges: Boolean): FunSpec {
        var returnType = TypeUtil.parseMetaType(typeMirror!!, isIterable, subEntitySimpleName).copy(nullable = true)

        val builder = FunSpec.builder("get" + accessorSuffix()).addModifiers(KModifier.PUBLIC).returns(returnType)


        if (isTypeOfSubEntity) {
            val castType = if (isSubEntityIsTypeParam) TypeUtil.listWithMutableMapStringObject() else TypeUtil.mutableMapStringObject()

            if (useMDocChanges) {
                builder.addCode(CodeBlock.builder().beginControlFlow("if(mDocChanges.containsKey(%N))", constantName)
                        .addStatement("return %T.fromMap(mDocChanges.get(%N) as %T)", subEntityTypeName, constantName, castType)
                        .endControlFlow().build())
            }
            builder.addCode(CodeBlock.builder()
                    .beginControlFlow("if(mDoc.containsKey(%N))", constantName)
                    .addStatement("return %T.fromMap(mDoc.get(%N) as %T)", subEntityTypeName, constantName, castType)
                    .endControlFlow().build())

            builder.addStatement("return null")
        } else {

            val forTypeConversion = evaluateClazzForTypeConversion()
            if (useMDocChanges) {
                builder.addCode(CodeBlock.builder().beginControlFlow("if(mDocChanges.containsKey(%N))", constantName)
                        .addStatement("return " + TypeConversionMethodsGeneration.READ_METHOD_NAME + "(mDocChanges.get(%N), %T::class)", constantName, forTypeConversion)
                        .endControlFlow().build())
            }

            builder.addCode(CodeBlock.builder().beginControlFlow("if(mDoc.containsKey(%N))", constantName).addStatement("return " + TypeConversionMethodsGeneration.READ_METHOD_NAME + "(mDoc.get(%N), %T::class)", constantName, forTypeConversion).endControlFlow().build())
//FIXME
//            if (forTypeConversion.is) {
//                builder.addStatement("return \$L.get(%T.class)", DefaultValue::class.java.canonicalName, forTypeConversion)
//            } else {
            builder.addStatement("return null")
//            }

        }

        return builder.build()
    }


    override fun setter(dbName: String?, entityTypeName: TypeName, useMDocChanges: Boolean): FunSpec {
        val fieldType = TypeUtil.parseMetaType(typeMirror!!, isIterable, subEntitySimpleName)
        val builder = FunSpec.builder("set" + accessorSuffix()).addModifiers(KModifier.PUBLIC).addParameter("value", fieldType).returns(entityTypeName)

        val docName = if (useMDocChanges) "mDocChanges" else "mDoc"

        if (isTypeOfSubEntity) {
            builder.addStatement("%N.put(%N, %T.toMap(value))", docName, constantName, subEntityTypeName)
            builder.addStatement("return this")
        } else {
            val forTypeConversion = evaluateClazzForTypeConversion()
            builder.addStatement("%N.put(%N, " + TypeConversionMethodsGeneration.WRITE_METHOD_NAME + "(value, %T::class))", docName, constantName, forTypeConversion)
            builder.addStatement("return this")
        }

        return builder.build()
    }

    override fun createFieldConstant(): List<PropertySpec> {

        val fieldAccessorConstant = PropertySpec.builder(constantName, String::class, KModifier.FINAL, KModifier.PUBLIC).initializer("%S", dbField).addAnnotation(JvmField::class).build()

        return listOf(fieldAccessorConstant)
    }

    private fun evaluateClazzForTypeConversion(): TypeName {
        return if (isIterable) {
            TypeUtil.string()
        } else TypeUtil.parseMetaType(typeMirror!!, isIterable, subEntitySimpleName)

    }
}
