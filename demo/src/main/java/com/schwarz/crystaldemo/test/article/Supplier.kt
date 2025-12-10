package schwarz.fwws.shared.model.article

import com.schwarz.crystalapi.Field
import com.schwarz.crystalapi.Fields
import com.schwarz.crystalapi.MapWrapper

@MapWrapper
@Fields(
    Field(name = "supplier_no", type = String::class),
    Field(name = "supplier_subrange", type = String::class),
    Field(name = "order_unit", type = String::class),
    Field(name = "min_ord_qty", type = String::class),
    Field(name = "delivery_unit", type = String::class),
    Field(name = "pre_vendor", type = String::class),
    Field(name = "dispo_subrange", type = String::class),
    Field(name = "supplier_name", type = String::class),
    Field(name = "ordering_area", type = String::class),
    Field(name = "ordering_area_name", type = String::class),
    Field(name = "kdis_flag", type = String::class),
    Field(name = "comm_category", type = String::class),
    Field(name = "landx", type = String::class),
)
open class Supplier
