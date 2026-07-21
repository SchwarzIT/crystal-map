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
import com.schwarz.crystalcore.util.unpurgeableEntriesWarning
import com.schwarz.crystalcore.util.writeTextIfChanged
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

class SchemaGenerator(
    path: String,
    val fileName: String,
    private val warn: (String) -> Unit = {},
) {
    private val path = File(path)

    // The schema file itself stays a plain List<EntitySchema> (its published
    // format); the sidecar carries the full previous-run model so the schema
    // survives deletion or corruption of the published file.
    private val modelFile = SideOutputModelFile(this.path, fileName)

    @Serializable
    internal data class SchemaModel(
        val entities: List<EntitySchema> = emptyList(),
        val sources: Map<String, List<String>> = emptyMap(),
    )

    private val jsonEntitySegments = mutableMapOf<String, EntitySchema>()
    private val entitySources = mutableMapOf<String, List<String>>()

    /**
     * With [mergeWithPrevious] the current run may only have seen a subset of
     * the entities (incremental KSP processing), so they are merged over the
     * previous run's model; entries whose source files disappeared or were
     * reprocessed without producing the entity again ([reprocessedFilePaths])
     * are purged. Without the flag the schema is rebuilt from this run's model
     * alone.
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
        val previousFileText = file.readTextOrNull()
        val previous =
            modelFile.loadPrevious(
                mergeWithPrevious,
                decode = { decodeJsonOrNull<SchemaModel>(it) },
                reconstruct = { legacyPreviousModel(it, previousFileText) },
            )
        val previousModel = previous.model
        if (mergeWithPrevious && previousModel == null && previousFileText != null) {
            // Neither the sidecar nor the schema file itself is decodable, and
            // rebuilding would degrade the schema to the reprocessed subset of
            // entities. Keep the complete-but-stale file until it is fixed or
            // deleted.
            warn(
                "Schema file $fileName is not decodable and no usable sidecar model exists; " +
                    "keeping the stale file. Delete it to regenerate from scratch.",
            )
            clearCollectedEntities()
            return
        }

        val merged =
            mergeSideOutputEntries(
                previousEntries = previousModel?.entities.orEmpty().associateBy { it.name },
                previousSources = previousModel?.sources.orEmpty(),
                currentEntries = jsonEntitySegments,
                reprocessedFilePaths = reprocessedFilePaths,
                onUnpurgeableEntries = { warn(unpurgeableEntriesWarning(fileName, it)) },
            )
        if (merged.isEmpty() && !file.exists()) {
            clearCollectedEntities()
            return
        }
        val mergedSources =
            (previousModel?.sources.orEmpty().filterKeys { merged.containsKey(it) } + entitySources).toSortedMap()

        file.writeTextIfChanged(Json.encodeToString(merged.values.toList()), previousFileText)
        modelFile.persist(
            Json.encodeToString(SchemaModel(entities = merged.values.toList(), sources = mergedSources)),
            previous.text,
        )
        clearCollectedEntities()
    }

    // Sidecars written before the entities moved into the model held only the
    // source map; the entities of such runs are recovered from the schema file,
    // which is its own published model.
    private fun legacyPreviousModel(
        previousModelText: String?,
        previousFileText: String?,
    ): SchemaModel? =
        decodeJsonOrNull<List<EntitySchema>>(previousFileText)?.let {
            SchemaModel(
                entities = it,
                sources = decodeJsonOrNull<Map<String, List<String>>>(previousModelText).orEmpty(),
            )
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
