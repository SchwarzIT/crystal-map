package com.schwarz.crystalprocessor.generation.model

import com.schwarz.crystalapi.schema.*
import com.schwarz.crystalprocessor.model.entity.SchemaClassHolder
import com.schwarz.crystalprocessor.model.field.CblBaseFieldHolder
import com.schwarz.crystalprocessor.model.field.CblFieldHolder
import com.schwarz.crystalprocessor.model.typeconverter.TypeConverterHolderForEntityGeneration
import com.schwarz.crystalprocessor.util.ConversionUtil
import com.schwarz.crystalprocessor.util.TypeUtil
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

/**
 * This class is responsible for generating the Schema classes.
 *
 * To generate a SchemaClass, add the following annotation to your Class:
 * ```
 * @SchemaClass
 * ```
 * All the fields will then be generated into a new file.
 */
class SchemaGeneration {
    private val pathAttributeName = "path"
    fun generateModel(
        holder: SchemaClassHolder,
        schemaClassPaths: List<String>,
        typeConvertersByConvertedClass: Map<TypeName, TypeConverterHolderForEntityGeneration>
    ): FileSpec {
        val packageName = holder.sourcePackage
        val schemaClassName = holder.entitySimpleName

        val schemaClass: TypeSpec.Builder = buildSchemaClass(schemaClassName)

        buildAndAddFieldProperties(holder, schemaClass, schemaClassPaths, typeConvertersByConvertedClass)

        return FileSpec.builder(packageName, schemaClassName).addType(schemaClass.build()).build()
    }

    private fun buildSchemaClass(className: String): TypeSpec.Builder {
        val pathParameter = ParameterSpec.builder(pathAttributeName, String::class).defaultValue("%S", "").build()

        return TypeSpec.classBuilder(className)
            .addModifiers(KModifier.OPEN)
            .addSuperinterface(Schema::class)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(pathParameter)
                    .build()
            )
    }

    private fun buildAndAddFieldProperties(
        holder: SchemaClassHolder,
        schemaClass: TypeSpec.Builder,
        schemaClassPaths: List<String>,
        typeConvertersByConvertedClass: Map<TypeName, TypeConverterHolderForEntityGeneration>
    ) {
        buildAndAddConstantFieldProperties(holder, schemaClass, schemaClassPaths, typeConvertersByConvertedClass)
        buildAndAddNormalFieldProperties(holder, schemaClass, schemaClassPaths, typeConvertersByConvertedClass)
    }

    private fun buildAndAddConstantFieldProperties(
        holder: SchemaClassHolder,
        schemaClass: TypeSpec.Builder,
        schemaClassPaths: List<String>,
        typeConvertersByConvertedClass: Map<TypeName, TypeConverterHolderForEntityGeneration>
    ) {
        holder.fieldConstants.forEach { (fieldName, fieldObject) ->
            val defaultVariableName = "DEFAULT_${fieldObject.constantName}"

            val constantProperty = PropertySpec.builder(
                defaultVariableName,
                fieldObject.fieldType
            ).initializer(
                ConversionUtil.convertStringToDesiredFormat(
                    fieldObject.typeMirror,
                    fieldObject.constantValue
                )
            )

            schemaClass.addProperty(constantProperty.build())

            buildAndAddFieldProperty(
                schemaClass,
                fieldName,
                fieldObject,
                schemaClassPaths,
                typeConvertersByConvertedClass
            )
        }
    }

    private fun buildAndAddNormalFieldProperties(
        holder: SchemaClassHolder,
        schemaClass: TypeSpec.Builder,
        schemaClassPaths: List<String>,
        typeConvertersByConvertedClass: Map<TypeName, TypeConverterHolderForEntityGeneration>
    ) {
        holder.fields.forEach { (fieldName, fieldObject) ->
            buildAndAddFieldProperty(
                schemaClass,
                fieldName,
                fieldObject,
                schemaClassPaths,
                typeConvertersByConvertedClass
            )
        }
    }

    private fun buildAndAddFieldProperty(
        schemaClass: TypeSpec.Builder,
        fieldName: String,
        fieldObject: CblBaseFieldHolder,
        schemaClassPaths: List<String>,
        typeConvertersByConvertedClass: Map<TypeName, TypeConverterHolderForEntityGeneration>
    ): TypeSpec.Builder {
        val propertyType = typeConvertersByConvertedClass[fieldObject.typeMirror.asTypeName()]
        val isObject = schemaClassPaths.contains(fieldObject.typeMirror.toString())
        val hasProperty = propertyType != null
        val fieldType = getFieldType(fieldObject.isIterable, isObject, hasProperty)
        return schemaClass.addProperty(
            if (propertyType != null) {
                buildConverterFieldProperty(fieldObject, fieldName, propertyType, fieldType)
            } else {
                buildFieldProperty(fieldObject, fieldName, fieldType, isObject)
            }
        )
    }

    private fun buildFieldProperty(
        fieldObject: CblBaseFieldHolder,
        fieldName: String,
        outerType: ClassName,
        isObject: Boolean
    ): PropertySpec {
        val genericFieldType = getGenericFieldType(fieldObject)

        return PropertySpec.builder(
            fieldObject.accessorSuffix(),
            outerType.parameterizedBy(genericFieldType)
        ).initializer(
            createPropertyFormat(fieldName, genericFieldType, fieldObject.isIterable, isObject),
            outerType
        ).build()
    }

    private fun buildConverterFieldProperty(
        fieldObject: CblBaseFieldHolder,
        fieldName: String,
        propertyType: TypeConverterHolderForEntityGeneration,
        fieldType: ClassName
    ): PropertySpec {
        return PropertySpec.builder(
            fieldObject.accessorSuffix(),
            fieldType.parameterizedBy(propertyType.domainClassTypeName, propertyType.mapClassTypeName)
        ).initializer(
            buildConverterFormat(fieldName, propertyType),
            fieldType
        ).build()
    }

    private fun createPropertyFormat(
        fieldName: String,
        propertyType: TypeName,
        isIterable: Boolean,
        isObject: Boolean
    ): String {
        val propertyAccessPath =
            "if ($pathAttributeName.isBlank()) \"$fieldName\" else \"\$$pathAttributeName.$fieldName\""

        return when {
            isIterable && isObject -> buildObjectListFormat(propertyType, fieldName, propertyAccessPath)
            isObject -> buildObjectFormat(propertyType, fieldName, propertyAccessPath)
            else -> buildSimpleFormat(fieldName)
        }
    }

    private fun buildConverterFormat(fieldName: String, propertyType: TypeConverterHolderForEntityGeneration): String =
        """%T("$fieldName", $pathAttributeName, ${propertyType.instanceClassTypeName})"""

    private fun buildObjectListFormat(propertyType: TypeName, fieldName: String, propertyAccessPath: String): String =
        """%T(
            $propertyType($propertyAccessPath),
            "$fieldName",
            $pathAttributeName,
        )"""

    private fun buildSimpleFormat(fieldName: String): String =
        """%T("$fieldName", $pathAttributeName)"""

    private fun buildObjectFormat(propertyType: TypeName, fieldName: String, propertyAccessPath: String): String =
        """%T(
            $propertyType($propertyAccessPath),
            "$fieldName",
            $pathAttributeName,
        )"""

    private fun getFieldType(
        isIterable: Boolean,
        isObject: Boolean,
        hasProperty: Boolean
    ) = when {
        hasProperty && isIterable -> CMConverterList::class.asTypeName()
        hasProperty -> CMConverterField::class.asTypeName()
        isIterable && isObject -> CMObjectList::class.asTypeName()
        isIterable -> CMJsonList::class.asTypeName()
        isObject -> CMObjectField::class.asTypeName()
        else -> CMJsonField::class.asTypeName()
    }

    private fun getGenericFieldType(field: CblBaseFieldHolder): TypeName {
        val subEntity = (field as? CblFieldHolder)?.subEntitySimpleName

        return TypeUtil.parseMetaType(field.typeMirror, false, subEntity)
    }
}
