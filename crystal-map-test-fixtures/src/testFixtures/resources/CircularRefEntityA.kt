import com.schwarz.crystalapi.Entity
import com.schwarz.crystalapi.Field
import com.schwarz.crystalapi.Fields
import com.schwarz.crystalapi.GenerateAccessor

@Entity(database = "test_db")
@Fields(
    Field(name = "name", type = String::class),
    Field(name = "type", type = String::class, defaultValue = "circA", readonly = true)
)
open class CircularRefEntityA {
    companion object {
        @GenerateAccessor
        fun getB(): CircularRefEntityBEntity? {
            return null
        }
    }
}
