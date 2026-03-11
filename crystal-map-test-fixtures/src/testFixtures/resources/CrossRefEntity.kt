import com.schwarz.crystalapi.Entity
import com.schwarz.crystalapi.Field
import com.schwarz.crystalapi.Fields

@Entity(database = "test_db")
@Fields(
    Field(name = "name", type = String::class),
    Field(name = "wrapper", type = CrossRefWrapper::class),
    Field(name = "type", type = String::class, defaultValue = "crossRefEntity", readonly = true)
)
open class CrossRefEntity
