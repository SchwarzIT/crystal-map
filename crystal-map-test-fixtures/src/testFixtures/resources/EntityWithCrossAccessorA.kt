import com.schwarz.crystalapi.Entity
import com.schwarz.crystalapi.Field
import com.schwarz.crystalapi.Fields
import com.schwarz.crystalapi.GenerateAccessor

@Entity(database = "test_db")
@Fields(
    Field(name = "name", type = String::class),
    Field(name = "type", type = String::class, defaultValue = "entityA", readonly = true)
)
open class EntityWithCrossAccessorA {
    companion object {
        @GenerateAccessor
        fun findB(id: String?): EntityWithCrossAccessorBEntity? {
            return null
        }

        @GenerateAccessor
        fun findBs(ids: List<EntityWithCrossAccessorBEntity>?): EntityWithCrossAccessorBEntity? {
            return null
        }
    }
}
