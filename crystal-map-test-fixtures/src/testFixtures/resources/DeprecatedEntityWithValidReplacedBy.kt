import com.schwarz.crystalapi.Entity
import com.schwarz.crystalapi.Field
import com.schwarz.crystalapi.Fields
import com.schwarz.crystalapi.deprecated.Deprecated
import com.schwarz.crystalapi.deprecated.DeprecationType

@Entity(database = "test_db")
@Fields(
    Field(name = "name", type = String::class),
    Field(name = "type", type = String::class, defaultValue = "deprecatedValid", readonly = true)
)
@Deprecated(type = DeprecationType.ENTITY_DEPRECATION_NOT_IN_USE)
open class DeprecatedEntityWithValidReplacedBy
