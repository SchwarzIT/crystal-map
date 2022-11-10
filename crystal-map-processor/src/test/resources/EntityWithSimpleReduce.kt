import com.schwarz.crystalapi.*
import java.lang.String


@Entity(database = "mydb_db")
@Fields(
        Field(name = "name", type = String::class),
        Field(name = "type", type = String::class, defaultValue = "EntityWithSimpleReduce", readonly = true),
        Field(name = "identifiers", type = String::class, list = true),
        Field(name = "map", type = Map::class, list = true)
)
@Reduces(
        Reduce(namePrefix = "Small", include = ["name", "type"])
)
open class EntityWithSimpleReduce {

}
