package com.schwarz.crystalprocessor.meta

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.schwarz.crystalapi.deprecated.DeprecationType
import com.schwarz.crystalprocessor.model.deprecated.DeprecatedModel
import com.schwarz.crystalprocessor.model.entity.BaseEntityHolder
import com.schwarz.crystalprocessor.model.field.CblBaseFieldHolder
import com.schwarz.crystalprocessor.model.query.CblQueryHolder
import com.schwarz.crystalapi.schema.*
import java.io.File

class SchemaGenerator(path: String, val fileName: String) {

    private val path = File(path)

    private val jsonEntitySegments = mutableMapOf<String, EntitySchema>()

    fun generate() {
        path.mkdirs()
        val mapper = ObjectMapper().registerModule(KotlinModule())
        File(path, fileName).writeText(mapper.writeValueAsString(jsonEntitySegments.values))
    }

    fun addEntity(entityHolder: BaseEntityHolder) {
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

    private fun DeprecatedModel.deprecatedToSchema(): DeprecatedSchema = DeprecatedSchema(replacedBy = this.replacedByTypeMirror.toString(), inUse = this.deprecationType == DeprecationType.FIELD_DEPRECATION || this.deprecationType == DeprecationType.ENTITY_DEPRECATION, deprecatedFields = this.deprecatedFields.values.map { DeprecatedFields(field = it.field, replacedBy = it.replacedBy, inUse = it.inUse) })

    private fun List<CblBaseFieldHolder>.fieldsToSchemaList(): List<Fields> = map { Fields(dbField = it.dbField, fieldType = it.fieldType.toString(), isIterable = it.isIterable, isConstant = it.isConstant, defaultValue = it.defaultValue) }

    private fun List<CblQueryHolder>.queriesToSchemaList(): List<Queries> = map { Queries(it.fields.asList()) }
}
