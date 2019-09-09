package schwarz.fwws.shared.model.article

import kaufland.com.coachbasebinderapi.Field
import kaufland.com.coachbasebinderapi.Fields
import kaufland.com.coachbasebinderapi.MapWrapper
import java.util.*

@MapWrapper
@Fields(
        Field(name = "promotion_no", type = String::class),
        Field(name = "start_date", type = Date::class),
        Field(name = "end_date", type = Date::class)
)
open class Promotion {

}