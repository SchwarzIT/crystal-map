package com.schwarz.crystalcore.documentation

import com.schwarz.crystalcore.model.entity.BaseEntityHolder
import com.squareup.kotlinpoet.TypeName
import j2html.TagCreator.*
import j2html.tags.DomContent
import java.io.File

class EntityRelationshipGenerator(path: String, fileName: String) {
    private val path = File(path)
    private val file = File(path, fileName)

    private val docuEntityNodes = mutableMapOf<String, DomContent>()
    private val docuEntityEdges = mutableMapOf<String, List<String>>()

    fun generate() {
        path.mkdirs()

        val documentBuilder = StringBuilder()
        documentBuilder.append("graph ER {\n")
        documentBuilder.append("node [shape=diamond];\n")
        documentBuilder.append(renderRelationshipDiamonds())
        documentBuilder.append("\n")
        documentBuilder.append(renderEntityNodes())
        documentBuilder.append("\n")
        documentBuilder.append(renderRelationships())
        documentBuilder.append("\n")
        documentBuilder.append("fontsize=12;\n")
        documentBuilder.append("}\n")

        file.writeText(documentBuilder.toString())
    }

    private fun renderRelationshipDiamonds(): String {
        return docuEntityEdges
            .toSortedMap()
            .filter { it.value.isNotEmpty() }
            .map { "  ${it.key}_has  [label=\"has\"];\n" }
            .joinToString("")
    }

    private fun renderEntityNodes(): String {
        return docuEntityNodes
            .toSortedMap()
            .map { renderEntityNode(it) }.joinToString("\n\n")
    }

    private fun renderEntityNode(node: Map.Entry<String, DomContent>): String {
        val nodeBuilder = StringBuilder()
        nodeBuilder.append("node [shape=plain]\n")
        nodeBuilder.append("  rankdir=LR;\n")
        nodeBuilder.append("  ${node.key} [label=<\n")
        nodeBuilder.append("  ${node.value.render()}\n")
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
                            .map { text(it.value.dbField + " : " + extractClassName(it.value.fieldType)) }
                            .zip(generateSequence { rawHtml("<br/>") }.asIterable())
                            .flatMap { listOf(it.first, it.second) }
                            .toTypedArray()
                    )
                )
            ).attr("border", "0").attr("cellborder", "1").attr("cellspacing", "1").attr("cellpadding", "5")

        docuEntityEdges[entityHolder.sourceClazzSimpleName] =
            entityHolder.fields
                .filter {
                    !it.value.fieldType.toString().startsWith("java") &&
                        !it.value.fieldType.toString().startsWith("kotlin") &&
                        !it.value.fieldType.toString().startsWith("org.threeten")
                }
                .map { extractClassName(it.value.fieldType) }
    }

    fun extractClassName(fullClassName: TypeName): String = fullClassName.toString().split(".").last()

    private fun renderRelationships(): String {
        return docuEntityEdges
            .toSortedMap()
            .filter { it.value.isNotEmpty() }
            .map { edge -> "${edge.key} -- ${edge.key}_has;\n" }
            .joinToString("") +
            docuEntityEdges
                .filter { it.value.isNotEmpty() }
                .map { edge -> edge.value.map { "${edge.key}_has -- $it;\n" } }
                .flatten()
                .joinToString("")
    }
}
