package schwarz.fwws.shared.model.article

import com.schwarz.crystalapi.Field
import com.schwarz.crystalapi.Fields
import com.schwarz.crystalapi.MapWrapper

@MapWrapper
@Fields(
    Field(name = "emp_article_no", type = String::class),
    Field(name = "emp_quant", type = String::class),
    Field(name = "emp_unit", type = String::class)
)
open class Empties
