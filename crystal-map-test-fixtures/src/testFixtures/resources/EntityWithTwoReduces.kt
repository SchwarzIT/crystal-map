import com.schwarz.crystalapi.*

@Entity(database = "test_db")
@Fields(
    Field(name = "name", type = String::class),
    Field(name = "description", type = String::class),
    Field(name = "count", type = Number::class),
    Field(name = "type", type = String::class, defaultValue = "entityTwoReduces", readonly = true)
)
@Reduces(
    Reduce(namePrefix = "Small", include = ["name", "type"]),
    Reduce(namePrefix = "Medium", include = ["name", "description", "type"])
)
open class EntityWithTwoReduces
