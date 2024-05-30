package com.schwarz.crystaldemo.customtypes

import com.schwarz.crystalapi.ITypeConverter
import com.schwarz.crystalapi.TypeConverter
import java.time.OffsetDateTime

@TypeConverter
open class OffsetDateTimeConverter : ITypeConverter<OffsetDateTime, String> {
    override fun write(value: OffsetDateTime?): String? = value?.toString()

    override fun read(value: String?): OffsetDateTime? = value?.let { OffsetDateTime.parse(it) }
}
