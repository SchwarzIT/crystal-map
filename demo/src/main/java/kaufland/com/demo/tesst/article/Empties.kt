package schwarz.fwws.shared.model.article

import kaufland.com.coachbasebinderapi.Field
import kaufland.com.coachbasebinderapi.Fields
import kaufland.com.coachbasebinderapi.MapWrapper

@MapWrapper
@Fields(
        Field(name = "emp_article_no", type = String::class),
        Field(name = "emp_quant", type = String::class),
        Field(name = "emp_unit", type = String::class)
)
open class Empties {

}