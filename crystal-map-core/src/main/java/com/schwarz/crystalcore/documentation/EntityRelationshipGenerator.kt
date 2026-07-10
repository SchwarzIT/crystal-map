package com.schwarz.crystalcore.documentation

import com.schwarz.crystalcore.model.entity.BaseEntityHolder
import com.schwarz.crystalcore.util.EmbeddedModel
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

    private val docuEntityNodes = mutableMapOf<String, DomContent>()
    private val docuEntityEdges = mutableMapOf<String, List<String>>()

    @Serializable
    internal data class RelationshipModel(
        val nodes: Map<String, String>,
        val edges: Map<String, List<String>>,
    )

    fun generate() {
        path.mkdirs()

        val previous = loadPreviousModel()
        val merged =
            RelationshipModel(
                nodes = (previous.nodes + docuEntityNodes.mapValues { it.value.render() }).toSortedMap(),
                edges = (previous.edges + docuEntityEdges).toSortedMap(),
            )

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
        documentBuilder.append(EmbeddedModel.embed(MODEL_PREFIX, "", Json.encodeToString(merged)))
        documentBuilder.append("\n")

        file.writeTextIfChanged(documentBuilder.toString())
        docuEntityNodes.clear()
        docuEntityEdges.clear()
    }

    private fun loadPreviousModel(): RelationshipModel =
        if (file.exists()) {
            EmbeddedModel
                .extract(file.readText(), MODEL_PREFIX, "")
                ?.let { runCatching { Json.decodeFromString<RelationshipModel>(it) }.getOrNull() }
                ?: EMPTY_MODEL
        } else {
            EMPTY_MODEL
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

    fun <T> addEntityNodes(entityHolder: BaseEntityHolder<T>) {
        if (docuEntityNodes.containsKey(entityHolder.sourceClazzSimpleName) ||
            docuEntityEdges.containsKey(entityHolder.sourceClazzSimpleName)
        ) {
            return
        }

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

    companion object {
        private const val MODEL_PREFIX = "// crystal-map-model:"
        private val EMPTY_MODEL = RelationshipModel(emptyMap(), emptyMap())
    }

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
