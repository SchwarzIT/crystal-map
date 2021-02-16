package com.kaufland.meta

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.kaufland.model.deprecated.DeprecatedModel
import com.kaufland.model.entity.BaseEntityHolder
import com.kaufland.model.field.CblFieldHolder
import com.kaufland.model.query.CblQueryHolder
import kaufland.com.coachbasebinderapi.deprecated.Deprecated
import kaufland.com.coachbasebinderapi.scheme.*
import java.io.File

class SchemeGenerator(path: String, val fileName: String) {

    private val path = File(path)

    private val jsonEntitySegments = mutableMapOf<String, EntityScheme>()

    fun generate() {
        path.mkdirs()
        val mapper = ObjectMapper().registerModule(KotlinModule())
        File(path, fileName).writeText(mapper.writeValueAsString(jsonEntitySegments.values))
    }

    fun addEntity(entityHolder: BaseEntityHolder) {
        if (jsonEntitySegments.containsKey(entityHolder.sourceClazzSimpleName)) {
            return
        }

        val entityScheme = EntityScheme(
                name = entityHolder.sourceClazzSimpleName,
                fields = entityHolder.fields.fieldsToSchemeList(),
                basedOn = entityHolder.basedOn.map { it.sourceClazzSimpleName },
                queries = entityHolder.queries.queriesToSchemeList(),
                deprecatedScheme = entityHolder.deprecated?.deprecatedToScheme()
        )
        jsonEntitySegments[entityHolder.sourceClazzSimpleName] = entityScheme
    }

    private fun DeprecatedModel.deprecatedToScheme() : DeprecatedScheme = DeprecatedScheme(replacedBy = this.replacedByTypeMirror.toString(), replaceIn = this.replacedIn, deprecatedFields = this.deprecatedFields.values.map { DeprecatedFields(field = it.field, replacedBy = it.replacedBy, replaceIn = it.replacedIn) })

    private fun MutableMap<String, CblFieldHolder>.fieldsToSchemeList(): List<Fields> = map { Fields(dbField = it.value.dbField, fieldType = it.value.fieldType.toString(), isIterable = it.value.isIterable, isConstant = it.value.isConstant, defaultValue = it.value.defaultValue) }

    private fun List<CblQueryHolder>.queriesToSchemeList(): List<Queries> = map { Queries(it.fields.asList()) }


}