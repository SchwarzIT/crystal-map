import kaufland.com.coachbasebinderapi.Entity
import kaufland.com.coachbasebinderapi.Field
import kaufland.com.coachbasebinderapi.Fields
import kaufland.com.coachbasebinderapi.deprecated.Deprecated
import kaufland.com.coachbasebinderapi.deprecated.DeprecatedField
import java.lang.String


@Entity(database = "mydb_db")
@Fields(
    Field(name = "name", type = String::class),
    Field(name = "type", type = String::class, defaultValue = "entityWithQueries", readonly = true),
    Field(name = "identifiers", type = String::class, list = true)
)
@Deprecated(fields = [DeprecatedField("name", replacedBy = "identifiers")])
open class EntityWithDeprecatedFields