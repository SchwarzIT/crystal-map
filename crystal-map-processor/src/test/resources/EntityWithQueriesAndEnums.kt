import com.schwarz.crystalapi.Entity
import com.schwarz.crystalapi.Field
import com.schwarz.crystalapi.Fields
import com.schwarz.crystalapi.MapWrapper
import com.schwarz.crystalapi.query.Queries
import com.schwarz.crystalapi.query.Query

enum class TestEnum {
        TEST_VALUE1,
        TEST_VALUE2
}

@Entity(database = "mydb_db")
@MapWrapper
@Fields(
        Field(name = "type", type = String::class, defaultValue = "entityWithQueries", readonly = true),
        Field(name = "enumField", type = TestEnum::class)
)
@Queries(
        Query(fields = ["type", "enumField"])
)
open class EntityWithQueriesAndEnums
