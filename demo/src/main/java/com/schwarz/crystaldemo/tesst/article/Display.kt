package schwarz.fwws.shared.model.article

import com.schwarz.crystalapi.Field
import com.schwarz.crystalapi.Fields
import com.schwarz.crystalapi.MapWrapper

@MapWrapper
@Fields(
    Field(name = "comp_article", type = String::class),
    Field(name = "comp_qty", type = String::class),
    Field(name = "comp_unit", type = String::class)
)
open class Display
