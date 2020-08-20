package kaufland.com.demo.entity

import com.couchbase.lite.Blob
import kaufland.com.coachbasebinderapi.*
import kaufland.com.coachbasebinderapi.query.Queries
import kaufland.com.coachbasebinderapi.query.Query

@Entity(database = "mydb_db")
@MapWrapper
@Fields(
        Field(name = "type", type = String::class, defaultValue = "something", readonly = true),
        Field(name = "just_some_class_with_a_very_long_name", type = JustSomeClassWithAVeryLongName::class, list = true)
)
open class TestClass