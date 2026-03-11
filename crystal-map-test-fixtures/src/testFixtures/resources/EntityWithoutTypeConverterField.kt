import com.schwarz.crystalapi.Entity
import com.schwarz.crystalapi.Field
import com.schwarz.crystalapi.Fields

@Entity(database = "test_db")
@Fields(
    Field(name = "title", type = String::class),
    Field(name = "count", type = Number::class),
    Field(name = "type", type = String::class, defaultValue = "withoutTC", readonly = true)
)
open class EntityWithoutTypeConverterField
