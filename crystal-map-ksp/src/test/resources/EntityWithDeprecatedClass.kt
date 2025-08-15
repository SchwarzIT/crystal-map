import com.schwarz.crystalapi.*
import com.schwarz.crystalapi.deprecated.Deprecated
import com.schwarz.crystalapi.deprecated.DeprecatedField
import com.schwarz.crystalapi.deprecated.DeprecationType
import com.schwarz.crystalapi.query.Queries
import com.schwarz.crystalapi.query.Query
import java.lang.String


@Entity(database = "mydb_db")
@Fields(
    Field(name = "name", type = String::class),
    Field(name = "type", type = String::class, defaultValue = "entityWithQueries", readonly = true),
    Field(name = "identifiers", type = String::class, list = true)
)
@Deprecated(type = DeprecationType.ENTITY_DEPRECATION_NOT_IN_USE)
@Queries(
    Query(fields = ["type"]), Query(fields = ["type", "name"])
)
@Reduces(
    Reduce(namePrefix = "Lighter", include = ["name"], includeQueries = false, includeDocId = false)
)
@DocId("myProduct:%type%:%name%")
open class EntityWithDeprecatedClass