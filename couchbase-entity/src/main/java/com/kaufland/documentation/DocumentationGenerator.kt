package com.kaufland.documentation

import com.kaufland.model.entity.BaseEntityHolder
import com.kaufland.model.field.CblBaseFieldHolder
import com.kaufland.util.TypeUtil
import j2html.TagCreator.*
import j2html.tags.DomContent
import java.io.File
import javax.lang.model.type.TypeMirror

class DocumentationGenerator(path: String, fileName: String) {

    private val path = File(path)

    private val file = File(path, fileName)

    private val docuEntitySegments = mutableListOf<DomContent>()

    fun generate() {
        val document = html(
                head(
                        title("EntityFramework Entities"),
                        style("<style>\n" +
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
                                "}\n" +
                                "</style>")
                ),
                body(
                        main(attrs("#main.content"),
                                div(*docuEntitySegments.toTypedArray())
                        )
                )
        ).renderFormatted()



        path.mkdirs()
        file.writeText(document)
    }

    fun addEntitySegments(entityHolder: BaseEntityHolder) {
        docuEntitySegments.add(div().with(h1(entityHolder.sourceClazzSimpleName), br(), table(attrs(".table"), thead(*createTableHead()), tbody(
                each(entityHolder.fields) { field ->
                    tr(
                            *parseField(field.value)
                    )
                }, each(entityHolder.fieldConstants) { field ->
            tr(*parseField(field.value))
        }
        ))))
    }

    private fun createTableHead(): Array<DomContent> {
        return arrayOf(th("Fieldname"), th("Type"), th("DefaultValue"), th("IsConstant"), th("Comment"))
    }

    private fun parseField(fields: CblBaseFieldHolder): Array<DomContent> {
        return arrayOf(td(fields.dbField), td(buildDisplayableType(fields.typeMirror, fields.isIterable)), td(fields.defaultValue), td(if (fields.isConstant) "X" else ""), td(fields.comment))
    }

    private fun buildDisplayableType(type : TypeMirror, iterable : Boolean) : String{
        val simpleName = TypeUtil.getSimpleName(type)
        return if(iterable){
            "List<$simpleName>"
        }else{
            simpleName
        }
    }



}