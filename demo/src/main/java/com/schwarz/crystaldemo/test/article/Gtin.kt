package schwarz.fwws.shared.model.article

import com.schwarz.crystalapi.Field
import com.schwarz.crystalapi.Fields
import com.schwarz.crystalapi.MapWrapper

@MapWrapper
@Fields(
    Field(name = "gtin_no", type = String::class),
    Field(name = "gtin_category", type = String::class),
    Field(name = "main_gtin", type = Boolean::class),
)
open class Gtin
