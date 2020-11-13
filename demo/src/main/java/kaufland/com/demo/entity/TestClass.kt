package kaufland.com.demo.entity

import com.couchbase.lite.Blob
import kaufland.com.coachbasebinderapi.*
import kaufland.com.coachbasebinderapi.query.Queries
import kaufland.com.coachbasebinderapi.query.Query
import kaufland.com.demo.MainActivity

@Entity(database = "mydb_db")
@MapWrapper
@Fields(
        Field(name = "type", type = String::class, defaultValue = "something", readonly = true),
        Field(name = "defaultBoolean", type = Boolean::class, defaultValue = "true", readonly = true),
        Field(name = "just_some_class_with_a_very_long_name", type = JustSomeClassWithAVeryLongName::class, list = true),
        Field(name = "map", type = Map::class, list = true)
)
@BasedOn(BaseModel::class, AnotherBaseModel::class)
open class TestClass