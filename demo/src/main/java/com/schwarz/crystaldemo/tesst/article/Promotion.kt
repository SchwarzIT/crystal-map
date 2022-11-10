package schwarz.fwws.shared.model.article

import com.schwarz.crystalapi.Field
import com.schwarz.crystalapi.Fields
import com.schwarz.crystalapi.MapWrapper
import java.util.*

@MapWrapper
@Fields(
    Field(name = "promotion_no", type = String::class),
    Field(name = "start_date", type = Date::class),
    Field(name = "end_date", type = Date::class)
)
open class Promotion
