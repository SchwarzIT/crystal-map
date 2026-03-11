import com.schwarz.crystalapi.Entity
import com.schwarz.crystalapi.Field
import com.schwarz.crystalapi.Fields
import java.time.OffsetDateTime

@Entity(database = "test_db")
@Fields(
    Field(name = "name", type = String::class),
    Field(name = "created_at", type = OffsetDateTime::class),
    Field(name = "type", type = String::class, defaultValue = "withTC", readonly = true)
)
open class EntityWithTypeConverterField
