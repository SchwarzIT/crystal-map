package com.schwarz.crystalcore.documentation

import com.schwarz.crystalcore.model.entity.BaseEntityHolder
import com.schwarz.crystalcore.util.decodeJsonOrNull
import com.schwarz.crystalcore.util.mergeSideOutputEntries
import com.schwarz.crystalcore.util.readTextOrNull
import com.schwarz.crystalcore.util.writeTextIfChanged
import com.squareup.kotlinpoet.TypeName
import j2html.TagCreator.b
import j2html.TagCreator.rawHtml
import j2html.TagCreator.table
import j2html.TagCreator.td
import j2html.TagCreator.text
import j2html.TagCreator.th
import j2html.TagCreator.tr
import j2html.tags.DomContent
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

class EntityRelationshipGenerator(
    path: String,
    fileName: String,
) {
    private val path = File(path)
    private val file = File(path, fileName)

    // Persisted model of the last run (nodes, edges and their source files), so
    // partial (incremental) runs can merge and purge instead of shrinking the
    // graph to the reprocessed subset of entities. Deliberately not named
    // *.json: the versioning plugin parses every *.json file in its schema
    // directories.
    private val modelFile = File(path, "$fileName.model")

    private val docuEntityNodes = mutableMapOf<String, DomContent>()
    private val docuEntityEdges = mutableMapOf<String, List<String>>()
    private val docuEntitySources = mutableMapOf<String, List<String>>()

    @Serializable
    internal data class RelationshipModel(
        val nodes: Map<String, String>,
        val edges: Map<String, List<String>>,
        val sources: Map<String, List<String>> = emptyMap(),
    )

    /**
     * Renders the relationship graph. With [mergeWithPrevious] the current run
     * may only have seen a subset of the entities (incremental KSP processing),
     * so nodes and edges are merged over the persisted model of the last run;
     * entries whose source files disappeared or were reprocessed without
     * producing the entity again ([reprocessedFilePaths]) are purged. Without
     * the flag the graph is rebuilt from this run's model alone.
     */
    fun generate(
        mergeWithPrevious: Boolean = false,
        reprocessedFilePaths: Set<String> = emptySet(),
    ) {
        val previousModelText = if (mergeWithPrevious) modelFile.readTextOrNull() else null
        val previous = decodeJsonOrNull<RelationshipModel>(previousModelText)
        if (mergeWithPrevious && previous == null && !currentRunCoversExistingFile()) {
            // The existing graph predates the persisted model (or the model is
            // corrupt), and this run saw fewer entities than the graph lists, so
            // it cannot rebuild the full graph. Keep the complete-but-stale file;
            // the next run that covers all entities rebuilds it and restores the
            // model.
            clearCollectedEntities()
            return
        }

        val mergedNodes =
            mergeSideOutputEntries(
                previousEntries = previous?.nodes ?: emptyMap(),
                previousSources = previous?.sources ?: emptyMap(),
                currentEntries = docuEntityNodes.mapValues { it.value.render() },
                reprocessedFilePaths = reprocessedFilePaths,
            )
        val merged =
            RelationshipModel(
                nodes = mergedNodes,
                edges =
                    (
                        (previous?.edges ?: emptyMap()).filterKeys { mergedNodes.containsKey(it) } + docuEntityEdges
                    ).toSortedMap(),
                sources =
                    (
                        (previous?.sources ?: emptyMap()).filterKeys { mergedNodes.containsKey(it) } + docuEntitySources
                    ).toSortedMap(),
            )
        if (mergeWithPrevious && file.exists() && merged == previous) {
            // Nothing changed: skip rendering and writing entirely.
            clearCollectedEntities()
            return
        }

        path.mkdirs()

        val documentBuilder = StringBuilder()
        documentBuilder.append("graph ER {\n")
        documentBuilder.append("node [shape=diamond];\n")
        documentBuilder.append(renderRelationshipDiamonds(merged.edges))
        documentBuilder.append("\n")
        documentBuilder.append(renderEntityNodes(merged.nodes))
        documentBuilder.append("\n")
        documentBuilder.append(renderRelationships(merged.edges))
        documentBuilder.append("\n")
        documentBuilder.append("fontsize=12;\n")
        documentBuilder.append("}\n")

        file.writeTextIfChanged(documentBuilder.toString())
        modelFile.writeTextIfChanged(
            Json.encodeToString(merged),
            previousModelText ?: modelFile.readTextOrNull(),
        )
        clearCollectedEntities()
    }

    private fun clearCollectedEntities() {
        docuEntityNodes.clear()
        docuEntityEdges.clear()
        docuEntitySources.clear()
    }

    // Every entity node block contains exactly one "[label=<" marker, so
    // counting it approximates how many entities the existing graph describes.
    private fun currentRunCoversExistingFile(): Boolean {
        val existing = file.readTextOrNull() ?: return true
        val documentedEntities = NODE_LABEL_MARKER.toRegex(RegexOption.LITERAL).findAll(existing).count()
        return docuEntityNodes.size >= documentedEntities
    }

    private companion object {
        private const val NODE_LABEL_MARKER = "[label=<"
    }

    private fun renderRelationshipDiamonds(edges: Map<String, List<String>>): String =
        edges
            .filter { it.value.isNotEmpty() }
            .map { "  ${it.key}_has  [label=\"has\"];\n" }
            .joinToString("")

    private fun renderEntityNodes(nodes: Map<String, String>): String =
        nodes
            .map { renderEntityNode(it) }
            .joinToString("\n\n")

    private fun renderEntityNode(node: Map.Entry<String, String>): String {
        val nodeBuilder = StringBuilder()
        nodeBuilder.append("node [shape=plain]\n")
        nodeBuilder.append("  rankdir=LR;\n")
        nodeBuilder.append("  ${node.key} [label=<\n")
        nodeBuilder.append("  ${node.value}\n")
        nodeBuilder.append("  >];\n")
        return nodeBuilder.toString()
    }

    fun <T> addEntityNodes(
        entityHolder: BaseEntityHolder<T>,
        sourcePaths: List<String> = emptyList(),
    ) {
        if (docuEntityNodes.containsKey(entityHolder.sourceClazzSimpleName) ||
            docuEntityEdges.containsKey(entityHolder.sourceClazzSimpleName)
        ) {
            return
        }

        docuEntitySources[entityHolder.sourceClazzSimpleName] = sourcePaths

        docuEntityNodes[entityHolder.sourceClazzSimpleName] =
            table(
                th(td(b(entityHolder.sourceClazzSimpleName))),
                tr(
                    td(
                        *entityHolder.fields
                            .map {
                                text(
                                    it.value.dbField + " : " + extractClassName(it.value.fieldType),
                                )
                            }.zip(generateSequence { rawHtml("<br/>") }.asIterable())
                            .flatMap { listOf(it.first, it.second) }
                            .toTypedArray(),
                    ),
                ),
            ).attr(
                "border",
                "0",
            ).attr("cellborder", "1")
                .attr("cellspacing", "1")
                .attr("cellpadding", "5")

        docuEntityEdges[entityHolder.sourceClazzSimpleName] =
            entityHolder.fields
                .filter {
                    !it.value.fieldType
                        .toString()
                        .startsWith("java") &&
                        !it.value.fieldType
                            .toString()
                            .startsWith("kotlin") &&
                        !it.value.fieldType
                            .toString()
                            .startsWith("org.threeten")
                }.map { extractClassName(it.value.fieldType) }
    }

    fun extractClassName(fullClassName: TypeName): String =
        fullClassName.toString().split(".").last()

    private fun renderRelationships(edges: Map<String, List<String>>): String =
        edges
            .filter { it.value.isNotEmpty() }
            .map { edge -> "${edge.key} -- ${edge.key}_has;\n" }
            .joinToString("") +
            edges
                .filter { it.value.isNotEmpty() }
                .map { edge -> edge.value.map { "${edge.key}_has -- $it;\n" } }
                .flatten()
                .joinToString("")
}
