package kaufland.com.demo.entity

import com.couchbase.lite.Blob
import kaufland.com.coachbasebinderapi.*

import kaufland.com.demo.Application

@Entity(database = "mydb_db")
@Fields(
        Field(name = "type", type = String::class, defaultValue = "product", readonly = true),
        Field(name = "name", type = String::class),
        Field("comments", type = UserComment::class, list = true),
        Field(name = "image", type = Blob::class),
        Field(name = "identifiers", type = String::class, list = true)
)
open class Product