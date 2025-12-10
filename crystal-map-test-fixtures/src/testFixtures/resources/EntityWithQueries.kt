import com.schwarz.crystalapi.Entity
import com.schwarz.crystalapi.Field
import com.schwarz.crystalapi.Fields
import com.schwarz.crystalapi.query.Queries
import com.schwarz.crystalapi.query.Query


@Entity(database = "mydb_db")
@Fields(
    Field(name = "name", type = String::class),
    Field(name = "type", type = String::class, defaultValue = "entityWithQueries", readonly = true),
    Field(name = "identifiers", type = String::class, list = true)
)
@Queries(
    Query(fields = ["type"])
)
open class EntityWithQueries