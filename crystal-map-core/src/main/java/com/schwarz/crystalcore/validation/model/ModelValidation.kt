package com.schwarz.crystalcore.validation.model

import com.schwarz.crystalapi.Reduce
import com.schwarz.crystalapi.TypeConverter
import com.schwarz.crystalapi.deprecated.DeprecatedField
import com.schwarz.crystalcore.ILogger
import com.schwarz.crystalcore.model.entity.BaseEntityHolder
import com.schwarz.crystalcore.model.entity.EntityHolder
import com.schwarz.crystalcore.model.entity.WrapperEntityHolder
import com.schwarz.crystalcore.model.typeconverter.ImportedTypeConverterHolder
import com.schwarz.crystalcore.model.typeconverter.TypeConverterHolder
import com.schwarz.crystalcore.model.typeconverter.TypeConverterHolderForEntityGeneration
import com.schwarz.crystalcore.model.typeconverter.nonConvertibleClassesTypeNames
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName

class ModelValidation<T>(
    val logger: ILogger<T>,
    val wrapperModels: MutableMap<String, WrapperEntityHolder<T>>,
    val entityModels: MutableMap<String, EntityHolder<T>>,
    val typeConverterModels: List<TypeConverterHolder>,
    val importedTypeConverterModels: List<ImportedTypeConverterHolder>
) {

    private val allTypeConverterModels: List<TypeConverterHolderForEntityGeneration> =
        typeConverterModels + importedTypeConverterModels

    private fun validateQuery(baseEntityHolder: BaseEntityHolder<T>) {
        for (query in baseEntityHolder.queries) {
            for (field in query.fields) {
                if (!baseEntityHolder.fields.containsKey(field) && !baseEntityHolder.fieldConstants.containsKey(
                        field
                    )
                ) {
                    baseEntityHolder.sourceElement.logError(
                        logger,
                        "query param [$field] is not a part of this entity"
                    )
                }
            }
        }
    }

    private fun validateDeprecated(baseEntityHolder: BaseEntityHolder<T>) {
        baseEntityHolder.deprecated?.let { deprecated ->
            if(deprecated.replacedBy != ""){
                deprecated.replacedBy.apply {
                    if (this != Void::class.java.canonicalName && !wrapperModels.containsKey(this) && !entityModels.containsKey(
                            this
                        )
                    ) {
                        baseEntityHolder.sourceElement.logError(
                            logger,
                            "replacement [$this] is not an entity/wrapper"
                        )
                    }
                }
            }

            validateDeprecatedFields(
                deprecated.deprecatedFields,
                deprecated.replacedBy,
                baseEntityHolder
            )
        }
    }

    private fun validateDeprecatedFields(
        deprecatedFields: Map<String, DeprecatedField>,
        replacingModel: String?,
        model: BaseEntityHolder<T>
    ) {
        val replacingModel: BaseEntityHolder<T>? = replacingModel?.let {
            wrapperModels[it] ?: entityModels[it]
        }

        val fieldsAccessorsDocId: List<String> =
            model.docId?.distinctFieldAccessors(model) ?: emptyList()
        for (field in deprecatedFields) {
            if (!model.fields.containsKey(field.key) && !model.fieldConstants.containsKey(field.key) && model.isReduced.not()) {
                model.sourceElement.logError(
                    logger,
                    "replacement field [${field.key}] does not exists"
                )
            }

            field.value.replacedBy?.let { replacement ->
                if (replacement.isNotEmpty()) {
                    val replacingIncludedInModel =
                        model.fields.containsKey(replacement) || model.fieldConstants.containsKey(
                            replacement
                        )
                    val replacementIncludedReplacingModel = replacingModel?.let {
                        it.fields.containsKey(replacement) || it.fieldConstants?.containsKey(
                            replacement
                        ) == true
                    }
                        ?: false

                    if (!replacingIncludedInModel && !replacementIncludedReplacingModel && model.isReduced.not()) {
                        model.sourceElement.logError(
                            logger,
                            "replacement [$replacement] for field [${field.key}] does not exists"
                        )
                    }
                }
            }

            if (fieldsAccessorsDocId.contains(field.key)) {
                model.sourceElement.logError(
                    logger,
                    "deprecated field is a part of DocId which is not possible since DocId is a final value. Use Deprecation on Entity/Wrapper level instead"
                )
            }
        }
    }

    private fun validateDocId(baseEntityHolder: BaseEntityHolder<T>) {
        baseEntityHolder.docId?.let {
            // we always need our variables between %
            if (it.pattern.count { it == '%' } % 2 != 0) {
                baseEntityHolder.sourceElement.logError(
                    logger,
                    "all variables in a DocId should be wrapped in % e.G. %variable%"
                )
            }

            for (segment in it.segments) {
                for (field in segment.fields) {
                    if (!baseEntityHolder.fieldConstants.containsKey(field) && !baseEntityHolder.fields.containsKey(
                            field
                        )
                    ) {
                        baseEntityHolder.sourceElement.logError(
                            logger,
                            "field [$field] for DocId generation does not exists"
                        )
                    }
                }
                segment.customSegment?.apply {
                    if (!it.customSegments.containsKey(name)) {
                        baseEntityHolder.sourceElement.logError(
                            logger,
                            "DocIdSegment annotated [$name] not found in DocId"
                        )
                    }
                }

                if (segment.customSegment == null && (
                    segment.segment.contains('(') || segment.segment.contains(
                            ')'
                        )
                    )
                ) {
                    baseEntityHolder.sourceElement.logError(
                        logger,
                        "It looks like you try to use a DocIdSegment which not exists"
                    )
                }
            }
        }
    }

    private fun validateReduces(baseEntityHolder: BaseEntityHolder<T>) {
        val allFieldNames =
            listOf(baseEntityHolder.fieldConstants.keys, baseEntityHolder.fields.keys).flatten()
        baseEntityHolder.reducesModels.forEach {
            it.includedElements.forEach {
                if (allFieldNames.contains(it).not()) {
                    baseEntityHolder.sourceElement.logError(
                        logger,
                        "[$it] ${Reduce::class.java.name} can only contains fields which belongs to the root of the parent element"
                    )
                }
            }
        }
    }

    private fun validateTypeConversionTypes(baseEntityHolder: BaseEntityHolder<T>) {
        val allTypeConversionFields =
            baseEntityHolder.fields.filter { !it.value.isTypeOfSubEntity && !it.value.isNonConvertibleClass }

        allTypeConversionFields.forEach { fieldEntry ->

            val fieldType = fieldEntry.value.fieldType
            val rawFieldType = (fieldType as? ParameterizedTypeName)?.rawType ?: fieldType

            if (allTypeConverterModels.all { it.domainClassTypeName != rawFieldType }) {
                baseEntityHolder.sourceElement.logError(
                    logger,
                    "[${baseEntityHolder.entitySimpleName}] No ${TypeConverter::class.java.name} found for Type ${fieldEntry.value.fieldType}"
                )
            }
        }
    }

    private fun validateTypeConversions() {
        typeConverterModels.forEach {
            if (!nonConvertibleClassesTypeNames.contains(it.mapClassTypeName)) {
                logger.error(
                    "Invalid map type ${it.mapClassTypeName} found in TypeConverter ${it.classTypeName}. Should be one of $nonConvertibleClassesTypeNames",
                    null
                )
            }
        }

        val typeConverterMap: MutableMap<ClassName, TypeConverterHolderForEntityGeneration> =
            importedTypeConverterModels.associateBy { it.domainClassTypeName }.toMutableMap()

        typeConverterModels.forEach {
            val existingTypeConverter = typeConverterMap[it.domainClassTypeName]
            if (existingTypeConverter != null) {
                logger.error(
                    "Duplicate TypeConverters for domain class ${it.domainClassTypeName}. " +
                        "Cannot add ${it.classTypeName} since already defined by ${existingTypeConverter.instanceClassTypeName}",
                    null
                )
            } else {
                typeConverterMap.put(it.domainClassTypeName, it)
            }
        }
    }

    fun postValidate(): Boolean {
        validateTypeConversions()

        for (wrapper in wrapperModels) {
            validateQuery(wrapper.value)
            validateDeprecated(wrapper.value)
            validateDocId(wrapper.value)
            validateReduces(wrapper.value)
            validateTypeConversionTypes(wrapper.value)
        }

        for (entity in entityModels) {
            validateQuery(entity.value)
            validateDeprecated(entity.value)
            validateDocId(entity.value)
            validateReduces(entity.value)
            validateTypeConversionTypes(entity.value)
        }

        return logger.hasErrors().not()
    }
}
