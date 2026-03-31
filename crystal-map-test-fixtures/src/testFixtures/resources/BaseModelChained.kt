import com.schwarz.crystalapi.BaseModel
import com.schwarz.crystalapi.BasedOn
import com.schwarz.crystalapi.Field
import com.schwarz.crystalapi.Fields

@BaseModel
@BasedOn(BaseModelSingle::class)
@Fields(
    Field(name = "chained_field", type = String::class)
)
open class BaseModelChained
