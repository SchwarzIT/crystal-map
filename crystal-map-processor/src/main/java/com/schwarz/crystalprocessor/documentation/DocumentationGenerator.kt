package com.schwarz.crystalprocessor.documentation

import com.schwarz.crystalprocessor.model.entity.BaseEntityHolder
import com.schwarz.crystalprocessor.model.field.CblBaseFieldHolder
import com.schwarz.crystalprocessor.model.source.ISourceModel
import com.schwarz.crystalprocessor.util.TypeUtil
import j2html.TagCreator.*
import j2html.tags.DomContent
import j2html.tags.UnescapedText
import java.io.File
import javax.lang.model.type.TypeMirror

class DocumentationGenerator(path: String, fileName: String) {

    private val path = File(path)

    private val file = File(path, fileName)

    private val docuEntitySegments = mutableMapOf<String, DomContent>()

    fun generate() {
        val document = html(
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
                        "}\n"
                )
            ),
            body(
                main(
                    attrs("#main.content"),
                    div(*docuEntitySegments.values.toTypedArray())
                )
            )
        ).renderFormatted()

        path.mkdirs()
        file.writeText(document)
    }

    fun addEntitySegments(entityHolder: BaseEntityHolder) {
        if (docuEntitySegments.containsKey(entityHolder.sourceClazzSimpleName)) {
            return
        }

        val btnWithSectionLink = "<button onclick=\"alert(window.location.protocol + '//' + window.location.host + window.location.pathname + window.location.search + '#${entityHolder.sourceClazzSimpleName}');\">showLink</button>"
        docuEntitySegments[entityHolder.sourceClazzSimpleName] = div().withId(entityHolder.sourceClazzSimpleName).with(
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
                            *parseField(field.value)
                        )
                    },
                    each(entityHolder.fieldConstants) { field ->
                        tr(*parseField(field.value))
                    }
                )
            )
        )
    }

    private fun evaluateAvailableTypes(sourceElement: ISourceModel?): DomContent {
        val entitySymbol = UnescapedText("<small> Entity: ${if (sourceElement?.entityAnnotation != null) CHECKMARK_EMOJI else CROSSMARK_EMOJI} </small>")
        val wrapperSymbol = UnescapedText("<small> MapWrapper: ${if (sourceElement?.mapWrapperAnnotation != null) CHECKMARK_EMOJI else CROSSMARK_EMOJI} </small>")

        return table(attrs(".card"), tr(td(entitySymbol), td(wrapperSymbol)))
    }

    private fun createTableHead(): Array<DomContent> {
        return arrayOf(th("Fieldname"), th("Type"), th("DefaultValue"), th("IsConstant"), th("Comment"))
    }

    private fun parseField(fields: CblBaseFieldHolder): Array<DomContent> {
        return arrayOf(td(fields.dbField), td(buildDisplayableType(fields.typeMirror, fields.isIterable)), td(fields.defaultValue), td(if (fields.isConstant) "X" else ""), td(*buildComment(fields.comment)))
    }

    private fun buildComment(comments: Array<String>): Array<DomContent> {
        return comments
            .map { p(UnescapedText(it)) }
            .toTypedArray()
    }

    private fun buildDisplayableType(type: TypeMirror, iterable: Boolean): String {
        val simpleName = TypeUtil.getSimpleName(type)
        return if (iterable) {
            "List<$simpleName>"
        } else {
            simpleName
        }
    }

    companion object {
        private const val CHECKMARK_EMOJI = "&#9989;"
        private const val CROSSMARK_EMOJI = "&#10062;"
    }
}
