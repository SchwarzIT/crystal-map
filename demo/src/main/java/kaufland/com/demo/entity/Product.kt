package kaufland.com.demo.entity

import com.couchbase.lite.Blob
import kaufland.com.coachbasebinderapi.Entity
import kaufland.com.coachbasebinderapi.Field
import kaufland.com.coachbasebinderapi.Fields

@Entity(database = "mydb_db")
@Fields(
        Field(name = "type", type = String::class, defaultValue = "product", readonly = true),
        Field(name = "name", type = String::class),
        Field(name = "comments", type = UserComment::class, list = true),
        Field(name = "image", type = Blob::class),
        Field(name = "identifiers", type = String::class, list = true)
)
open class Product