package com.schwarz.crystaldemo.test.article

import com.schwarz.crystalapi.DocId
import com.schwarz.crystalapi.Entity
import com.schwarz.crystalapi.Field
import com.schwarz.crystalapi.Fields
import com.schwarz.crystalapi.SchemaClass
import schwarz.fwws.shared.model.article.Supplier
import java.util.Date

@Entity(type = Entity.Type.READONLY)
@Fields(
    Field(name = "storeId", type = String::class),
    Field(name = "type", type = String::class, defaultValue = StoreArticle.TYPE, readonly = true),
    Field(name = "store_no", type = String::class),
    Field(name = "article_no", type = String::class),
    Field(name = "remaining_days", type = Int::class),
    Field(name = "store_remaining_days", type = Int::class),
    Field(name = "customer_remaining_days", type = Int::class),
    Field(name = "pal_kolli_qty", type = String::class),
    Field(name = "article_protection", type = String::class),
    Field(name = "hpl_kolli_qty", type = String::class),
    Field(name = "sales_start_date", type = Date::class),
    Field(name = "sales_end_date", type = Date::class),
    Field(name = "discount_allowed", type = Boolean::class),
    Field(name = "min_stock", type = String::class),
    Field(name = "article_status", type = String::class),
    Field(name = "article_status_date", type = Date::class),
    Field(name = "kannlist_flag", type = String::class),
    Field(name = "hpl_log_flag", type = String::class),
    Field(name = "dispo_profile", type = String::class),
    Field(name = "listing_start_date", type = Date::class),
    Field(name = "listing_end_date", type = Date::class),
    Field(name = "src_of_supply", type = String::class),
    Field(name = "dispo_sign", type = String::class),
    Field(name = "max_stock", type = String::class),
    Field(name = "supplier", type = Supplier::class),
)
@DocId("art:%storeId%:%article_no%")
@SchemaClass
open class StoreArticle {
    companion object {
        const val PREFIX: String = "storearticle"
        const val TYPE: String = "StoreArticle"

        fun documentId(
            storeId: String,
            article_no: String,
        ): String = "$PREFIX:$storeId:$article_no"
    }

//    override fun documentId(): String {
//        return Companion.documentId(storeId, article_no)
//    }
}
