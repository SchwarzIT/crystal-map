package com.schwarz.crystalcore.documentation

import com.schwarz.crystalcore.model.entity.BaseEntityHolder
import com.schwarz.crystalcore.model.field.CblBaseFieldHolder
import com.schwarz.crystalcore.model.source.ISourceModel
import j2html.TagCreator.attrs
import j2html.TagCreator.body
import j2html.TagCreator.br
import j2html.TagCreator.div
import j2html.TagCreator.each
import j2html.TagCreator.h1
import j2html.TagCreator.head
import j2html.TagCreator.html
import j2html.TagCreator.main
import j2html.TagCreator.p
import j2html.TagCreator.rawHtml
import j2html.TagCreator.style
import j2html.TagCreator.table
import j2html.TagCreator.tbody
import j2html.TagCreator.td
import j2html.TagCreator.th
import j2html.TagCreator.thead
import j2html.TagCreator.title
import j2html.TagCreator.tr
import j2html.tags.DomContent
import j2html.tags.UnescapedText
import java.io.File

class DocumentationGenerator(
    path: String,
    fileName: String,
) {
    private val path = File(path)

    private val file = File(path, fileName)

    private val docuEntitySegments = mutableMapOf<String, DomContent>()

    fun generate() {
        val document =
            html(
                head(
                    title("EntityFramework Entities"),
                    style(
                        "\n" +
                            ".card {\n" +
                            "  width: 100%;\n" +
                            "  box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2);\n" +
                            "  padding: 16px;\n" +
                            "  text-align: center;\n" +
                            "  background-color: #f2f2f2;\n" +
                            "}\n" +
                            ".table {\n" +
                            "  font-family: \"Trebuchet MS\", Arial, Helvetica, sans-serif;\n" +
                            "  border-collapse: collapse;\n" +
                            "  width: 100%;\n" +
                            "}\n" +
                            "\n" +
                            ".table td, .table th {\n" +
                            "  border: 1px solid #ddd;\n" +
                            "  padding: 8px;\n" +
                            "}\n" +
                            "\n" +
                            ".table tr:nth-child(even){background-color: #f2f2f2;}\n" +
                            "\n" +
                            ".table tr:hover {background-color: #ddd;}\n" +
                            "\n" +
                            ".table th {\n" +
                            "  padding-top: 12px;\n" +
                            "  padding-bottom: 12px;\n" +
                            "  text-align: left;\n" +
                            "  background-color: #4CAF50;\n" +
                            "  color: white;\n" +
                            "}\n",
                    ),
                ),
                body(
                    main(
                        attrs("#main.content"),
                        div(*docuEntitySegments.values.toTypedArray()),
                    ),
                ),
            ).renderFormatted()

        path.mkdirs()
        file.writeText(document)
    }

    fun <T> addEntitySegments(entityHolder: BaseEntityHolder<T>) {
        if (docuEntitySegments.containsKey(entityHolder.sourceClazzSimpleName)) {
            return
        }

        val btnWithSectionLink = "<button onclick=\"alert(window.location.protocol + '//' + window.location.host " +
            "+ window.location.pathname + window.location.search + '#${entityHolder.sourceClazzSimpleName}');\">showLink</button>"
        docuEntitySegments[entityHolder.sourceClazzSimpleName] =
            div().withId(entityHolder.sourceClazzSimpleName).with(
                h1(entityHolder.sourceClazzSimpleName),
                rawHtml(btnWithSectionLink),
                evaluateAvailableTypes(entityHolder.sourceElement),
                br(),
                *buildComment(entityHolder.comment),
                br(),
                table(
                    attrs(".table"),
                    thead(*createTableHead()),
                    tbody(
                        each(entityHolder.fields) { field ->
                            tr(
                                *parseField(field.value),
                            )
                        },
                        each(entityHolder.fieldConstants) { field ->
                            tr(*parseField(field.value))
                        },
                    ),
                ),
            )
    }

    private fun <T> evaluateAvailableTypes(sourceElement: ISourceModel<T>?): DomContent {
        val entitySymbol =
            UnescapedText(
                "<small> Entity: ${if (sourceElement?.entityAnnotation != null) CHECKMARK_EMOJI else CROSSMARK_EMOJI} </small>",
            )
        val wrapperSymbol =
            UnescapedText(
                "<small> MapWrapper: ${if (sourceElement?.mapWrapperAnnotation != null) CHECKMARK_EMOJI else CROSSMARK_EMOJI} </small>",
            )

        return table(attrs(".card"), tr(td(entitySymbol), td(wrapperSymbol)))
    }

    private fun createTableHead(): Array<DomContent> =
        arrayOf(th("Fieldname"), th("Type"), th("DefaultValue"), th("IsConstant"), th("Comment"))

    private fun parseField(fields: CblBaseFieldHolder): Array<DomContent> =
        arrayOf(
            td(fields.dbField),
            td(buildDisplayableType(fields.simpleName, fields.isIterable)),
            td(fields.defaultValue),
            td(if (fields.isConstant) "X" else ""),
            td(*buildComment(fields.comment)),
        )

    private fun buildComment(comments: Array<String>): Array<DomContent> =
        comments
            .map { p(UnescapedText(it)) }
            .toTypedArray()

    private fun buildDisplayableType(
        simpleName: String,
        iterable: Boolean,
    ): String =
        if (iterable) {
            "List<$simpleName>"
        } else {
            simpleName
        }

    companion object {
        private const val CHECKMARK_EMOJI = "&#9989;"
        private const val CROSSMARK_EMOJI = "&#10062;"
    }
}
