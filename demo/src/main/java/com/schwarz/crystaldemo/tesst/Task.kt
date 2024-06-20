package schwarz.fwws.shared.model

import com.schwarz.crystalapi.*
import com.schwarz.crystalapi.query.Queries
import com.schwarz.crystalapi.query.Query

@Entity
@Fields(
    Field(name = "storeId", type = String::class),
    Field(name = "item_type", type = String::class),
    Field(name = "type", type = String::class, defaultValue = Task.TYPE, readonly = true),
    Field(name = "process", type = String::class),
    Field(name = "article_no", type = String::class),
    Field(name = "expiration", type = Long::class)
)
@Queries(
    Query(fields = ["type"]),
    Query(fields = ["type", "item_type"])
)
open class Task {

    companion object {

        const val TYPE: String = "Task"

        @GenerateAccessor
        const val PREFIX: String = "task"

        fun documentId(storeId: String, article_no: String, process: String, uuid: String): String {
            return "$PREFIX:$storeId:$article_no:$process:$uuid"
        }

        @GenerateAccessor
        fun ultraComplexQuery(storeId: String): String {
            return ""
        }

        @GenerateAccessor
        suspend fun suspendingUltraComplexQueryReturningList(storeId: String): List<String> {
            return listOf("")
        }

        @GenerateAccessor(isNullableSuspendFun = true)
        suspend fun suspendingUltraComplexQueryReturningNullableList(storeId: String): List<String>? {
            return null
        }
    }
//
//    override fun documentId(): String {
//        return Companion.documentId(storeId, article_no, process, UUID.randomUUID().toString())
//    }
}
