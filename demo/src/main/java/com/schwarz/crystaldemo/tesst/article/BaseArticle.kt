package schwarz.fwws.shared.model.article

import com.schwarz.crystalapi.Field
import com.schwarz.crystalapi.Fields
import com.schwarz.crystalapi.MapWrapper
import com.schwarz.crystalapi.deprecated.Deprecated
import com.schwarz.crystalapi.deprecated.DeprecatedField

@MapWrapper(modifierOpen = true)
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
@Deprecated(replacedBy = StoreArticle::class, fields = [DeprecatedField(field = "base_unit", replacedBy = "kolli_qty")])
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
