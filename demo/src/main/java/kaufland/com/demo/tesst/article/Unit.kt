package schwarz.fwws.shared.model.article

import kaufland.com.coachbasebinderapi.Field
import kaufland.com.coachbasebinderapi.Fields
import kaufland.com.coachbasebinderapi.MapWrapper

@MapWrapper
@Fields(
    Field(name = "unit", type = String::class),
    Field(name = "numerator", type = Double::class),
    Field(name = "denominator", type = Double::class),
    Field(name = "gtin_no", type = String::class),
    Field(name = "gtin_category", type = String::class),
    Field(name = "other_gtins", type = Gtin::class, list = true),
    Field(name = "promotions", type = Promotion::class, list = true),
    Field(name = "empties", type = Empties::class, list = true)
)
open class Unit
