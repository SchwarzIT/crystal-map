package com.schwarz.crystaldemo.test

import com.schwarz.crystalapi.Entity
import com.schwarz.crystalapi.Field
import com.schwarz.crystalapi.Fields
import com.schwarz.crystalapi.GenerateAccessor
import com.schwarz.crystalapi.query.Queries
import com.schwarz.crystalapi.query.Query

@Entity
@Fields(
    Field(name = "storeId", type = String::class),
    Field(name = "item_type", type = String::class),
    Field(name = "type", type = String::class, defaultValue = Task.TYPE, readonly = true),
    Field(name = "process", type = String::class),
    Field(name = "article_no", type = String::class),
    Field(name = "expiration", type = Long::class),
)
@Queries(
    Query(fields = ["type"]),
    Query(fields = ["type", "item_type"]),
)
open class Task {
    companion object {
        const val TYPE: String = "Task"

        @GenerateAccessor
        const val PREFIX: String = "task"

        fun documentId(
            storeId: String,
            article_no: String,
            process: String,
            uuid: String,
        ): String = "$PREFIX:$storeId:$article_no:$process:$uuid"

        @GenerateAccessor
        fun ultraComplexQuery(storeId: String): String = ""

        @GenerateAccessor
        fun ultraComplexQueryReturningEntity(storeId: String): List<TaskEntity> = emptyList()

        @GenerateAccessor
        suspend fun suspendingUltraComplexQueryReturningList(storeId: String): List<String> =
            listOf("")

        @GenerateAccessor()
        suspend fun suspendingUltraComplexQueryReturningNullableList(
            storeId: String,
        ): List<String>? = null

        @GenerateAccessor()
        suspend fun suspendingUltraComplexQueryReturningEntity(storeId: String): List<TaskEntity> =
            emptyList()

        @GenerateAccessor()
        suspend fun suspendingUltraComplexQueryWithAVeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeryLongName(
            storeId: String,
        ): List<TaskEntity> = emptyList()
    }
//
//    override fun documentId(): String {
//        return Companion.documentId(storeId, article_no, process, UUID.randomUUID().toString())
//    }
}
