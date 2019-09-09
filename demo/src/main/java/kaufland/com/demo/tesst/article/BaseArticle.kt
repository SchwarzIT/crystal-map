package schwarz.fwws.shared.model.article

import kaufland.com.coachbasebinderapi.Field
import kaufland.com.coachbasebinderapi.Fields
import kaufland.com.coachbasebinderapi.MapWrapper
import schwarz.fwws.shared.model.DWG
import schwarz.fwws.shared.model.Model

@MapWrapper
@Fields(
        Field(name = "country", type = String::class),
        Field(name = "type", type = String::class, defaultValue = BaseArticle.TYPE, readonly = true),
        Field(name = "article_no", type = String::class),
        Field(name = "unit", type = Unit::class),
        Field(name = "other_units", type = Unit::class, list = true),
        Field(name = "bbtyp", type = String::class),
        Field(name = "general_article_group", type = String::class),
        Field(name = "article_group", type = String::class),
        Field(name = "base_unit", type = String::class),
        Field(name = "sub_article_group", type = String::class),
        Field(name = "sales_block", type = Boolean::class),
        Field(name = "child_protection", type = String::class),
        Field(name = "temp_sign", type = String::class),
        Field(name = "dang_goods", type = String::class),
        Field(name = "vintage", type = String::class),
        Field(name = "daily_article", type = Boolean::class),
        Field(name = "second_place", type = Boolean::class),
        Field(name = "article_short_text", type = String::class),
        Field(name = "article_text", type = String::class),
        Field(name = "selling_unit", type = String::class),
        Field(name = "vat_flag", type = String::class),
        Field(name = "displays", type = Display::class, list = true),
        Field(name = "kolli_qty", type = Int::class),
        Field(name = "dwg", type = String::class)
)
open class BaseArticle {
    companion object {
        const val PREFIX: String = "basearticle"
        const val TYPE: String = "BaseArticle"

        fun documentId(country: String, article_no: String): String {
            return "$PREFIX:$country:$article_no"
        }
    }

//    override fun documentId(): String {
//        return Companion.documentId(country, article_no)
//    }
}