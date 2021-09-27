package schwarz.fwws.shared.model.article

import kaufland.com.coachbasebinderapi.Field
import kaufland.com.coachbasebinderapi.Fields
import kaufland.com.coachbasebinderapi.MapWrapper

@MapWrapper
@Fields(
    Field(name = "gtin_no", type = String::class),
    Field(name = "gtin_category", type = String::class),
    Field(name = "main_gtin", type = Boolean::class)
)
open class Gtin
