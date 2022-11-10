package schwarz.fwws.shared.model

import com.schwarz.crystalapi.Field
import com.schwarz.crystalapi.Fields
import com.schwarz.crystalapi.MapWrapper
import java.util.*

@MapWrapper
@Fields(
    Field(name = "country", type = String::class),
    Field(name = "type", type = String::class, defaultValue = DWG.TYPE, readonly = true),
    Field(name = "name", type = String::class),
    Field(name = "color", type = String::class),
    Field(name = "icon", type = String::class),
    Field(name = "item_type", type = String::class),
    Field(name = "ordinal", type = Int::class)
)
open class DWG {
    companion object {
        const val TYPE: String = "DWG"
        const val PREFIX: String = "dwg"

        fun documentId(country: String, item_type: String): String {
            return "$PREFIX:$country:$item_type"
        }
    }

//    override fun documentId(): String {
//        return Companion.documentId(country, item_type)
//    }
}
