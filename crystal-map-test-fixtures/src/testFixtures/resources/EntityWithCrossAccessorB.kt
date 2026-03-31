import com.schwarz.crystalapi.Entity
import com.schwarz.crystalapi.Field
import com.schwarz.crystalapi.Fields

@Entity(database = "test_db")
@Fields(
    Field(name = "value", type = String::class),
    Field(name = "type", type = String::class, defaultValue = "entityB", readonly = true)
)
open class EntityWithCrossAccessorB
