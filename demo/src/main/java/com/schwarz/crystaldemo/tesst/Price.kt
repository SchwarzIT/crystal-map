package schwarz.fwws.shared.model

import com.schwarz.crystalapi.Field
import com.schwarz.crystalapi.Fields
import com.schwarz.crystalapi.MapWrapper
import com.schwarz.crystalapi.deprecated.Deprecated
import com.schwarz.crystalapi.deprecated.DeprecatedField
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
@Deprecated(fields = [DeprecatedField("sales_price", inUse = false)])
open class Price {
    companion object {
        const val TYPE: String = "Price"
        const val PREFIX: String = "price"

        fun documentId(
            storeId: String,
            articleNo: String,
            uuid: String
        ): String {
            return "$PREFIX:$storeId:$articleNo:$uuid"
        }
    }

//    override fun documentId(): String {
//        return Companion.documentId(storeId, article_no, UUID.randomUUID().toString())
//    }
}
