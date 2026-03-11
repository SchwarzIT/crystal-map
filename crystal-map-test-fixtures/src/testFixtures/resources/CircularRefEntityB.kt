import com.schwarz.crystalapi.Entity
import com.schwarz.crystalapi.Field
import com.schwarz.crystalapi.Fields
import com.schwarz.crystalapi.GenerateAccessor

@Entity(database = "test_db")
@Fields(
    Field(name = "value", type = String::class),
    Field(name = "type", type = String::class, defaultValue = "circB", readonly = true)
)
open class CircularRefEntityB {
    companion object {
        @GenerateAccessor
        fun getA(): CircularRefEntityAEntity? {
            return null
        }
    }
}
