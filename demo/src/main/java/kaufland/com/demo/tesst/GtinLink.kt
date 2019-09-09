package schwarz.fwws.shared.model

import kaufland.com.coachbasebinderapi.Field
import kaufland.com.coachbasebinderapi.Fields
import kaufland.com.coachbasebinderapi.MapWrapper
import java.util.*

@MapWrapper
@Fields(
        Field(name = "country", type = String::class),
        Field(name = "type", type = String::class, defaultValue = GtinLink.TYPE, readonly = true),
        Field(name = "gtin_no", type = String::class),
        Field(name = "article_no", type = String::class),
        Field(name = "main_gtin", type = Boolean::class)
)
open class GtinLink {
    companion object {
        const val PREFIX: String = "gtinlink"
        const val TYPE: String = "GtinLink"

        fun documentId(country: String, gtin: String): String {
            return "$PREFIX:$country:$gtin"
        }
    }

//    override fun documentId(): String {
//        return documentId(country, gtin_no)
//    }
}