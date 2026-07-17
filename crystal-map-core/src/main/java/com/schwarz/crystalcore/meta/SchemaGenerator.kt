package com.schwarz.crystalcore.meta

import com.schwarz.crystalapi.deprecated.DeprecationType
import com.schwarz.crystalapi.schema.DeprecatedFields
import com.schwarz.crystalapi.schema.DeprecatedSchema
import com.schwarz.crystalapi.schema.DocId
import com.schwarz.crystalapi.schema.EntitySchema
import com.schwarz.crystalapi.schema.Fields
import com.schwarz.crystalapi.schema.Queries
import com.schwarz.crystalcore.model.deprecated.DeprecatedModel
import com.schwarz.crystalcore.model.entity.BaseEntityHolder
import com.schwarz.crystalcore.model.field.CblBaseFieldHolder
import com.schwarz.crystalcore.model.query.CblQueryHolder
import com.schwarz.crystalcore.util.SideOutputModelFile
import com.schwarz.crystalcore.util.decodeJsonOrNull
import com.schwarz.crystalcore.util.mergeSideOutputEntries
import com.schwarz.crystalcore.util.readTextOrNull
import com.schwarz.crystalcore.util.writeTextIfChanged
import kotlinx.serialization.json.Json
import java.io.File

class SchemaGenerator(
    path: String,
    val fileName: String,
) {
    private val path = File(path)

    // The schema file itself stays a plain List<EntitySchema> (its published
    // format); the per-entity source files needed for merging live in the sidecar.
    private val modelFile = SideOutputModelFile(this.path, fileName)

    private val jsonEntitySegments = mutableMapOf<String, EntitySchema>()
    private val entitySources = mutableMapOf<String, List<String>>()

    /**
     * Writes the schema JSON. With [mergeWithPrevious] the current run may only
     * have seen a subset of the entities (incremental KSP processing), so they
     * are merged over the previously written file; entries whose source files
     * disappeared or were reprocessed without producing the entity again
     * ([reprocessedFilePaths]) are purged. Without the flag the schema is
     * rebuilt from this run's model alone.
     */
    fun generate(
        mergeWithPrevious: Boolean = false,
        reprocessedFilePaths: Set<String> = emptySet(),
    ) {
        if (jsonEntitySegments.isEmpty() && !mergeWithPrevious) {
            return
        }
        path.mkdirs()

        val file = File(path, fileName)
        val previousText = file.readTextOrNull()
        val previous = if (mergeWithPrevious) decodeJsonOrNull<List<EntitySchema>>(previousText) else null
        if (mergeWithPrevious && previousText != null && previous == null) {
            // An existing schema that no longer decodes cannot be merged, and
            // rebuilding would degrade it to the reprocessed subset of entities.
            // Keep the complete-but-stale file until it is fixed or deleted.
            clearCollectedEntities()
            return
        }
        val previousSourcesText = if (mergeWithPrevious) modelFile.readText() else null
        val previousSources = decodeJsonOrNull<Map<String, List<String>>>(previousSourcesText) ?: emptyMap()

        val merged =
            mergeSideOutputEntries(
                previousEntries = previous.orEmpty().associateBy { it.name },
                previousSources = previousSources,
                currentEntries = jsonEntitySegments,
                reprocessedFilePaths = reprocessedFilePaths,
            )
        if (merged.isEmpty() && !file.exists()) {
            clearCollectedEntities()
            return
        }
        val mergedSources =
            (previousSources.filterKeys { merged.containsKey(it) } + entitySources).toSortedMap()

        file.writeTextIfChanged(Json.encodeToString(merged.values.toList()), previousText)
        modelFile.persist(Json.encodeToString(mergedSources.toMap()), previousSourcesText)
        clearCollectedEntities()
    }

    private fun clearCollectedEntities() {
        jsonEntitySegments.clear()
        entitySources.clear()
    }

    fun <T> addEntity(
        entityHolder: BaseEntityHolder<T>,
        sourcePaths: List<String> = emptyList(),
    ) {
        if (jsonEntitySegments.containsKey(entityHolder.sourceClazzSimpleName)) {
            return
        }

        entitySources[entityHolder.sourceClazzSimpleName] = sourcePaths

        val entitySchema =
            EntitySchema(
                name = entityHolder.sourceClazzSimpleName,
                fields = entityHolder.allFields.fieldsToSchemaList(),
                basedOn = entityHolder.basedOn.map { it.sourceClazzSimpleName },
                queries = entityHolder.queries.queriesToSchemaList(),
                docId = entityHolder.docId?.let { DocId(it.pattern) },
                deprecatedSchema = entityHolder.deprecated?.deprecatedToSchema(),
            )
        jsonEntitySegments[entityHolder.sourceClazzSimpleName] = entitySchema
    }

    private fun DeprecatedModel.deprecatedToSchema(): DeprecatedSchema =
        DeprecatedSchema(
            replacedBy = this.replacedBy,
            inUse =
                this.deprecationType == DeprecationType.FIELD_DEPRECATION ||
                    this.deprecationType == DeprecationType.ENTITY_DEPRECATION,
            deprecatedFields =
                this.deprecatedFields.values.map {
                    DeprecatedFields(field = it.field, replacedBy = it.replacedBy, inUse = it.inUse)
                },
        )

    private fun List<CblBaseFieldHolder>.fieldsToSchemaList(): List<Fields> =
        map {
            Fields(
                dbField = it.dbField,
                fieldType = it.fieldType.toString(),
                isIterable = it.isIterable,
                isConstant = it.isConstant,
                defaultValue = it.defaultValue,
                mandatory = (if (it.mandatory) true else null),
            )
        }

    private fun List<CblQueryHolder>.queriesToSchemaList(): List<Queries> =
        map {
            Queries(it.fields.asList())
        }
}
