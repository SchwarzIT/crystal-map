import kaufland.com.coachbasebinderapi.*
import kotlin.String


@Entity(database = "mydb_db")
@Fields(
        Field(name = "name", type = String::class),
        Field(name = "type", type = String::class, defaultValue = "entityWithQueries", readonly = true),
        Field(name = "identifiers", type = String::class, list = true),
        Field(name = "map", type = Map::class, list = true)
)
@DocId("my:%name%:%type%:%this.custom(name, identifiers)%")
open class EntityWithDocIdAndDocIdSegments {

    companion object{
        @DocIdSegment
        fun custom(name : String?, comments: List<String>?) : String = "blub"
    }

}