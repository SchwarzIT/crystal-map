package kaufland.com.demo.entity

import com.couchbase.lite.Blob
import kaufland.com.coachbasebinderapi.*

@Entity(database = "mydb_db", modifierOpen = true)
@MapWrapper
@Fields(
        Field(name = "type", type = String::class, defaultValue = "something", readonly = true),
        Field(name = "defaultBoolean", type = Boolean::class, defaultValue = "true", readonly = true),
        Field(name = "just_some_class_with_a_very_long_name", type = JustSomeClassWithAVeryLongName::class, list = true),
        Field(name = "map", type = Map::class, list = true)
)
@BasedOn(AnotherBaseModel::class)
open class TestClass