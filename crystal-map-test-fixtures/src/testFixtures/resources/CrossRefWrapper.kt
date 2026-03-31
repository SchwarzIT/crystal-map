import com.schwarz.crystalapi.Field
import com.schwarz.crystalapi.Fields
import com.schwarz.crystalapi.MapWrapper

@MapWrapper
@Fields(
    Field(name = "label", type = String::class)
)
open class CrossRefWrapper
