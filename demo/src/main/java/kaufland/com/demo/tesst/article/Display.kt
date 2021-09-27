package schwarz.fwws.shared.model.article

import kaufland.com.coachbasebinderapi.Field
import kaufland.com.coachbasebinderapi.Fields
import kaufland.com.coachbasebinderapi.MapWrapper

@MapWrapper
@Fields(
    Field(name = "comp_article", type = String::class),
    Field(name = "comp_qty", type = String::class),
    Field(name = "comp_unit", type = String::class)
)
open class Display
