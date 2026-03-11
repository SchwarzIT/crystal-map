import com.schwarz.crystalapi.BasedOn
import com.schwarz.crystalapi.Entity
import com.schwarz.crystalapi.Field
import com.schwarz.crystalapi.Fields

@Entity(database = "test_db")
@BasedOn(BaseModelChained::class)
@Fields(
    Field(name = "leaf_field", type = String::class),
    Field(name = "type", type = String::class, defaultValue = "entityChained", readonly = true)
)
open class EntityWithChainedBasedOn
