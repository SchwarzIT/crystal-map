import com.schwarz.crystalapi.BaseModel
import com.schwarz.crystalapi.Field
import com.schwarz.crystalapi.Fields

@BaseModel
@Fields(
    Field(name = "base_field", type = String::class),
    Field(name = "base_number", type = Number::class)
)
open class BaseModelSingle
