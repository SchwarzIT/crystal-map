package com.schwarz.crystaldemo.customtypes

import com.couchbase.lite.Blob
import com.schwarz.crystalapi.ITypeConverter
import com.schwarz.crystalapi.TypeConverter

@TypeConverter
abstract class BlobConverter : ITypeConverter<Blob, String> {
    override fun write(value: Blob?): String? {
        TODO("Not yet implemented")
    }

    override fun read(value: String?): Blob? {
        TODO("Not yet implemented")
    }
}
