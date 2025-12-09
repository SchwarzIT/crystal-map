import com.schwarz.crystalapi.Entity
import com.schwarz.crystalapi.Field
import com.schwarz.crystalapi.Fields
import com.schwarz.crystalapi.deprecated.Deprecated
import com.schwarz.crystalapi.deprecated.DeprecatedField
import java.lang.String


@Entity(database = "mydb_db")
@Fields(
Field(name = "name", type = String::class),
        Field(name = "type", type = String::class, defaultValue = "entityWithQueries", readonly = true),
Field(name = "identifiers", type = String::class, list = true)
)
@Deprecated(fields = [DeprecatedField("name", replacedBy = "name2")])
open class EntityWithWrongConfiguredDeprecatedFields