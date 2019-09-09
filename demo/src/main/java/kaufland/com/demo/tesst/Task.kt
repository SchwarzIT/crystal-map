package schwarz.fwws.shared.model


import com.couchbase.lite.Blob
import kaufland.com.coachbasebinderapi.Entity
import kaufland.com.coachbasebinderapi.Field
import kaufland.com.coachbasebinderapi.Fields
import kaufland.com.coachbasebinderapi.MapWrapper
import kaufland.com.demo.Application
import kaufland.com.demo.entity.UserComment
import java.util.*

@MapWrapper
@Fields(
        Field(name = "storeId", type = String::class),
        Field(name = "item_type", type = String::class),
        Field(name = "type", type = String::class, defaultValue = Task.TYPE, readonly = true),
        Field(name = "process", type = String::class),
        Field(name = "article_no", type = String::class),
        Field(name = "expiration", type = Long::class)
)
open class Task {

    companion object {
        const val TYPE: String = "Task"
        const val PREFIX: String = "task"

        fun documentId(storeId: String, article_no: String, process: String, uuid: String): String {
            return "$PREFIX:$storeId:$article_no:$process:$uuid"
        }
    }
//
//    override fun documentId(): String {
//        return Companion.documentId(storeId, article_no, process, UUID.randomUUID().toString())
//    }
}