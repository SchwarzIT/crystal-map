package kaufland.com.demo.entity

import com.couchbase.lite.Blob
import kaufland.com.coachbasebinderapi.*
import kaufland.com.coachbasebinderapi.query.Queries
import kaufland.com.coachbasebinderapi.query.Query

@Entity(database = "mydb_db")
@Comment(["Hey, I just met you and this is crazy", "But here's my documentation, so read it maybe"])
@Fields(
        Field(name = "type", type = String::class, defaultValue = "product", readonly = true),
        Field(name = "name", type = String::class, comment = ["contains the product name", "and other infos"]),
        Field(name = "comments", type = UserComment::class, list = true),
        Field(name = "image", type = Blob::class),
        Field(name = "identifiers", type = String::class, list = true)
)
@Queries(
        Query(fields = ["type"])
)
@DocId(prefix = "myProduct", fields = ["type", "name"])
open class Product