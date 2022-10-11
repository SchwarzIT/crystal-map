package kaufland.com.demo.entity

import com.couchbase.lite.Blob
import kaufland.com.coachbasebinderapi.*
import kaufland.com.coachbasebinderapi.query.Queries
import kaufland.com.coachbasebinderapi.query.Query

@Entity(database = "mydb_db")
@MapWrapper
@Comment(["Hey, I just met you and this is crazy", "But here's my documentation, so read it maybe"])
@Fields(
        Field(name = "type", type = String::class, defaultValue = "product", readonly = true, comment = ["Document type"]),
        Field(name = "name", type = String::class, comment = ["contains the product name.", "and other infos"]),
        Field(name = "comments", type = UserComment::class, list = true, comment = ["I'm also comfortable with pseudo %2D placeholders"]),
        Field(name = "image", type = Blob::class),
        Field(name = "identifiers", type = String::class, list = true),
        Field(name = "category", type = ProductCategory::class),
)
@Queries(
        Query(fields = ["type"]),
        Query(fields = ["type", "category"])
)
@Reduces(
        Reduce(namePrefix = "Light", include = ["name", "type", "category", "image"]),
        Reduce(namePrefix = "Lighter", include = ["name"], includeQueries = false, includeDocId = false)
)
@DocId("myProduct:%type%:%name%:%custom(name)%")
open class Product {

    companion object {
        @DocIdSegment
        fun custom(name: String?): String = "${name}blub"
    }
}
