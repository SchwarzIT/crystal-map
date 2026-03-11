import com.schwarz.crystalapi.BasedOn
import com.schwarz.crystalapi.Entity
import com.schwarz.crystalapi.Field
import com.schwarz.crystalapi.Fields

@Entity(database = "test_db")
@BasedOn(BaseModelSingle::class)
@Fields(
    Field(name = "own_field", type = String::class),
    Field(name = "type", type = String::class, defaultValue = "entityWithBasedOn", readonly = true)
)
open class EntityWithBasedOnSingle
