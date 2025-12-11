package com.schwarz.crystaldemo.customtypes

import com.couchbase.lite.Blob
import com.couchbase.lite.internal.utils.JsonUtils
import com.schwarz.crystalapi.ITypeConverter
import com.schwarz.crystalapi.TypeConverter
import org.json.JSONObject
import java.util.Base64

@TypeConverter
abstract class BlobConverter : ITypeConverter<Blob, String> {
    override fun write(value: Blob?): String? =
        value?.let {
            JsonUtils
                .toJson(
                    mapOf(
                        "value" to Base64.getEncoder().encodeToString(it.content),
                        "contentType" to it.contentType,
                    ),
                ).toString()
        }

    override fun read(value: String?): Blob? =
        value?.let {
            val jsonObject = JSONObject(value)
            Blob(
                jsonObject.getString("contentType"),
                Base64.getDecoder().decode(jsonObject.getString("value")),
            )
        }
}
