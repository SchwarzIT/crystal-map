package schwarz.fwws.shared.model


import com.couchbase.lite.Blob
import kaufland.com.coachbasebinderapi.Field
import kaufland.com.coachbasebinderapi.Fields
import kaufland.com.coachbasebinderapi.MapWrapper
import kaufland.com.demo.entity.UserComment
import java.util.*

@MapWrapper
@Fields(
        Field(name = "storeId", type = String::class),
        Field(name = "article_no", type = String::class),
        Field(name = "type", type = String::class, defaultValue = Price.TYPE, readonly = true),
        Field(name = "condition_no", type = String::class),
        Field(name = "start_date", type = Date::class),
        Field(name = "end_date", type = Date::class),
        Field(name = "sales_price", type = String::class),
        Field(name = "currency_unit", type = String::class)
)
open class Price {
    companion object {
        const val TYPE: String = "Price"
        const val PREFIX: String = "price"

        fun documentId(storeId: String, articleNo: String, uuid: String): String {
            return "$PREFIX:$storeId:$articleNo:$uuid"
        }
    }

//    override fun documentId(): String {
//        return Companion.documentId(storeId, article_no, UUID.randomUUID().toString())
//    }
}