import kaufland.com.coachbasebinderapi.Entity
import kaufland.com.coachbasebinderapi.Field
import kaufland.com.coachbasebinderapi.Fields
import kaufland.com.coachbasebinderapi.query.Queries
import kaufland.com.coachbasebinderapi.query.Query

enum class TestEnum {
        TEST_VALUE1,
        TEST_VALUE2
}

@Entity(database = "mydb_db")
@Fields(
        Field(name = "type", type = String::class, defaultValue = "entityWithQueries", readonly = true),
        Field(name = "enumField", type = TestEnum::class)
)
@Queries(
        Query(fields = ["type", "enumField"])
)
open class EntityWithQueriesAndEnums
