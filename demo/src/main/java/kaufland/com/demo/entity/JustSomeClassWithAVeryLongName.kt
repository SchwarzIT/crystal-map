package kaufland.com.demo.entity

import com.couchbase.lite.Blob
import kaufland.com.coachbasebinderapi.*
import kaufland.com.coachbasebinderapi.query.Queries
import kaufland.com.coachbasebinderapi.query.Query

@Entity(database = "mydb_db")
@MapWrapper
@Fields(
        Field(name = "type", type = String::class, defaultValue = "something", readonly = true),
        Field(name = "name", type = String::class)
)
open class JustSomeClassWithAVeryLongName