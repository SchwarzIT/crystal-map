import com.schwarz.crystalapi.Entity
import com.schwarz.crystalapi.Field
import com.schwarz.crystalapi.Fields
import com.schwarz.crystalapi.GenerateAccessor
import java.lang.String


@Entity(database = "mydb_db")
@Fields(
        Field(name = "name", type = String::class),
        Field(name = "type", type = String::class, defaultValue = "entityWithQueries", readonly = true),
        Field(name = "identifiers", type = String::class, list = true),
        Field(name = "map", type = Map::class, list = true)
)
open class EntityWithGenerateAccessor {

    companion object {
        @GenerateAccessor
        fun someComplexQuery(foo: kotlin.String?): kotlin.String {
            return "333"
        }

        @GenerateAccessor
        fun moreComplexQueryE(value: List<EntityWithGenerateAccessor>?): EntityWithGenerateAccessor? {
            return null
        }

        @GenerateAccessor
        fun someComplexQuery3() {

        }
    }
}