package com.schwarz.crystalcore.meta

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.schwarz.crystalapi.deprecated.DeprecationType
import com.schwarz.crystalcore.model.deprecated.DeprecatedModel
import com.schwarz.crystalcore.model.entity.BaseEntityHolder
import com.schwarz.crystalcore.model.field.CblBaseFieldHolder
import com.schwarz.crystalcore.model.query.CblQueryHolder
import com.schwarz.crystalapi.schema.*
import java.io.File

class SchemaGenerator(path: String, val fileName: String) {

    private val path = File(path)

    private val jsonEntitySegments = mutableMapOf<String, EntitySchema>()

    fun generate() {
        path.mkdirs()
        val mapper = ObjectMapper().registerModule(
            KotlinModule.Builder()
                .withReflectionCacheSize(512)
                .configure(KotlinFeature.NullToEmptyCollection, false)
                .configure(KotlinFeature.NullToEmptyMap, false)
                .configure(KotlinFeature.NullIsSameAsDefault, false)
                .configure(KotlinFeature.SingletonSupport, false)
                .configure(KotlinFeature.StrictNullChecks, false)
                .build()
        )
        File(path, fileName).writeText(mapper.writeValueAsString(jsonEntitySegments.values))
    }

    fun <T> addEntity(entityHolder: BaseEntityHolder<T>) {
        if (jsonEntitySegments.containsKey(entityHolder.sourceClazzSimpleName)) {
            return
        }

        val entitySchema = EntitySchema(
            name = entityHolder.sourceClazzSimpleName,
            fields = entityHolder.allFields.fieldsToSchemaList(),
            basedOn = entityHolder.basedOn.map { it.sourceClazzSimpleName },
            queries = entityHolder.queries.queriesToSchemaList(),
            docId = entityHolder.docId?.let { DocId(it.pattern) },
            deprecatedSchema = entityHolder.deprecated?.deprecatedToSchema()
        )
        jsonEntitySegments[entityHolder.sourceClazzSimpleName] = entitySchema
    }

    private fun DeprecatedModel.deprecatedToSchema(): DeprecatedSchema = DeprecatedSchema(replacedBy = this.replacedBy, inUse = this.deprecationType == DeprecationType.FIELD_DEPRECATION || this.deprecationType == DeprecationType.ENTITY_DEPRECATION, deprecatedFields = this.deprecatedFields.values.map { DeprecatedFields(field = it.field, replacedBy = it.replacedBy, inUse = it.inUse) })

    private fun List<CblBaseFieldHolder>.fieldsToSchemaList(): List<Fields> = map { Fields(dbField = it.dbField, fieldType = it.fieldType.toString(), isIterable = it.isIterable, isConstant = it.isConstant, defaultValue = it.defaultValue, mandatory = (if (it.mandatory) true else null)) }

    private fun List<CblQueryHolder>.queriesToSchemaList(): List<Queries> = map { Queries(it.fields.asList()) }
}
