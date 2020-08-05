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

    val fieldType: TypeName = TypeUtil.parseMetaType(typeMirror, isIterable, subEntitySimpleName)

    init {
        if (allWrappers.contains(typeMirror.toString())) {

            subEntitySimpleName = TypeUtil.getSimpleName(typeMirror) + "Wrapper"
            subEntityPackage = TypeUtil.getPackage(typeMirror)
            isSubEntityIsTypeParam = field.list

        }
        if (field.list) {
            isIterable = true
        }
    }

    override fun property(dbName: String?, possibleOverrides: Set<String>, useMDocChanges: Boolean): PropertySpec {
        var returnType = TypeUtil.parseMetaType(typeMirror, isIterable, subEntitySimpleName).copy(nullable = true)

        var kmodifiers = ArrayList<KModifier>()
        kmodifiers.add(KModifier.PUBLIC)

        if(possibleOverrides.contains(accessorSuffix())){
            kmodifiers.add(KModifier.OVERRIDE)
        }

        val propertyBuilder = PropertySpec.builder(accessorSuffix(), returnType.copy(true),  *kmodifiers.toTypedArray()).mutable(true)


        val getter = FunSpec.getterBuilder()
        val setter = FunSpec.setterBuilder().addParameter("value", String::class)

        val docName = if (useMDocChanges) "mDocChanges" else "mDoc"

        if (isTypeOfSubEntity) {
            val castType = if (isSubEntityIsTypeParam) TypeUtil.listWithMutableMapStringAnyNullable() else TypeUtil.mutableMapStringAnyNullable()

            if (useMDocChanges) {
                getter.addCode(CodeBlock.builder().beginControlFlow("if(mDocChanges.containsKey(%N))", constantName)
                        .addStatement("return %T.fromMap(mDocChanges.get(%N) as %T)", subEntityTypeName, constantName, castType)
                        .endControlFlow().build())
            }
            getter.addCode(CodeBlock.builder()
                    .beginControlFlow("if(mDoc.containsKey(%N))", constantName)
                    .addStatement("return %T.fromMap(mDoc.get(%N) as %T)", subEntityTypeName, constantName, castType)
                    .endControlFlow().build())

            getter.addStatement("return null")

            setter.addStatement("%N.put(%N, %T.toMap(value))", docName, constantName, subEntityTypeName)
        } else {

            val forTypeConversion = evaluateClazzForTypeConversion()
            if (useMDocChanges) {
                getter.addCode(CodeBlock.builder().beginControlFlow("if(mDocChanges.containsKey(%N))", constantName)
                        .addStatement("return " + TypeConversionMethodsGeneration.READ_METHOD_NAME + "(mDocChanges.get(%N), %T::class)", constantName, forTypeConversion)
                        .endControlFlow().build())
            }

            getter.addCode(CodeBlock.builder().beginControlFlow("if(mDoc.containsKey(%N))", constantName).addStatement("return " + TypeConversionMethodsGeneration.READ_METHOD_NAME + "(mDoc.get(%N), %T::class)", constantName, forTypeConversion).endControlFlow().build())
//FIXME
//            if (forTypeConversion.is) {
//                builder.addStatement("return \$L.get(%T.class)", DefaultValue::class.java.canonicalName, forTypeConversion)
//            } else {
            getter.addStatement("return null")
//            }

            setter.addStatement("%N.put(%N, " + TypeConversionMethodsGeneration.WRITE_METHOD_NAME + "(value, %T::class))", docName, constantName, forTypeConversion)

        }

        return propertyBuilder.setter(setter.build()).getter(getter.build()).build()
    }

    override fun builderSetter(dbName: String?, packageName: String, entitySimpleName: String, useMDocChanges: Boolean): FunSpec? {
        val fieldType = TypeUtil.parseMetaType(typeMirror, isIterable, subEntitySimpleName)
        val builder = FunSpec.builder("set" + accessorSuffix().capitalize()).addModifiers(KModifier.PUBLIC).addParameter("value", fieldType).returns(ClassName(packageName, "${entitySimpleName}.Builder"))

        builder.addStatement("obj.${accessorSuffix()} = value")
        builder.addStatement("return this")

        return builder.build()
    }

    override fun createFieldConstant(): List<PropertySpec> {

        val fieldAccessorConstant = PropertySpec.builder(constantName, String::class, KModifier.FINAL, KModifier.PUBLIC).initializer("%S", dbField).addAnnotation(JvmField::class).build()

        return listOf(fieldAccessorConstant)
    }

    private fun evaluateClazzForTypeConversion(): TypeName {
        return if (isIterable) {
            TypeUtil.string()
        } else TypeUtil.parseMetaType(typeMirror, isIterable, subEntitySimpleName)

    }
}
