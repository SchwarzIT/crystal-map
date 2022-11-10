import com.schwarz.crystalapi.*
import com.schwarz.crystalapi.deprecated.Deprecated
import com.schwarz.crystalapi.deprecated.DeprecatedField
import java.lang.String


@Entity(database = "mydb_db")
@Fields(
    Field(name = "name", type = String::class),
    Field(name = "type", type = String::class, defaultValue = "entityWithQueries", readonly = true),
    Field(name = "identifiers", type = String::class, list = true)
)
@Deprecated(fields = [DeprecatedField("name", replacedBy = "identifiers")])
@Reduces(
    Reduce(include = ["type"], namePrefix = "reduced")
)
open class EntityWithDeprecatedFieldsAndReduce