package com.schwarz.crystalcore.documentation

import com.schwarz.crystalcore.model.entity.BaseEntityHolder
import com.schwarz.crystalcore.util.SideOutputModelFile
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

    private val modelFile = SideOutputModelFile(this.path, fileName)

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
        val previousModelText = if (mergeWithPrevious) modelFile.readText() else null
        val previous =
            decodeJsonOrNull<RelationshipModel>(previousModelText)
                ?: if (mergeWithPrevious) reconstructModelFromExistingFile() else null

        val mergedNodes =
            mergeSideOutputEntries(
                previousEntries = previous?.nodes ?: emptyMap(),
                previousSources = previous?.sources ?: emptyMap(),
                currentEntries = docuEntityNodes.mapValues { it.value.render() },
                reprocessedFilePaths = reprocessedFilePaths,
            )
        // Carried-over edges may still list entities that were purged in this
        // run; those targets must be dropped, or the rendered graph would show
        // the removed entity as an implicit (phantom) node.
        val purgedNodes = (previous?.nodes ?: emptyMap()).keys - mergedNodes.keys
        val merged =
            RelationshipModel(
                nodes = mergedNodes,
                edges =
                    (
                        (previous?.edges ?: emptyMap())
                            .filterKeys { mergedNodes.containsKey(it) }
                            .mapValues { (_, targets) -> targets.filterNot { it in purgedNodes } } +
                            docuEntityEdges
                    ).toSortedMap(),
                sources =
                    (
                        (previous?.sources ?: emptyMap()).filterKeys { mergedNodes.containsKey(it) } + docuEntitySources
                    ).toSortedMap(),
            )

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
        modelFile.persist(Json.encodeToString(merged), previousModelText)
        clearCollectedEntities()
    }

    private fun clearCollectedEntities() {
        docuEntityNodes.clear()
        docuEntityEdges.clear()
        docuEntitySources.clear()
    }

    // A graph written before the sidecar existed (or whose sidecar is corrupt) can
    // still be merged: this generator writes the .gv in a fixed format, so nodes
    // and edges are recovered from the file itself. Recovered entries carry no
    // source paths and are therefore kept conservatively by the merge until a
    // later run reprocesses them.
    private fun reconstructModelFromExistingFile(): RelationshipModel? {
        val existing = file.readTextOrNull() ?: return null
        val nodes =
            NODE_PATTERN
                .findAll(existing)
                .associate { it.groupValues[1] to it.groupValues[2].trim() }
        val edges = mutableMapOf<String, MutableList<String>>()
        for (match in EDGE_PATTERN.findAll(existing)) {
            edges.getOrPut(match.groupValues[1]) { mutableListOf() }.add(match.groupValues[2])
        }
        return RelationshipModel(
            nodes = nodes.toSortedMap(),
            edges = edges.mapValues { it.value.toList() }.toSortedMap(),
        )
    }

    private companion object {
        private val NODE_PATTERN = Regex("""(\w+) \[label=<\s*(.*?)\s*>];""", RegexOption.DOT_MATCHES_ALL)
        private val EDGE_PATTERN = Regex("""(\w+)_has -- (\w+);""")
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
