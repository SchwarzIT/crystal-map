package com.schwarz.crystaldummy.customtypes

import com.schwarz.crystalapi.ITypeConverter
import com.schwarz.crystalapi.TypeConverter
import java.time.Instant
import java.util.Date

@TypeConverter
abstract class DummyDateConverter : ITypeConverter<Date, Long> {
    override fun write(value: Date?): Long? =
        value?.toInstant()?.epochSecond

    override fun read(value: Long?): Date? = value?.let { Date.from(Instant.ofEpochSecond(it)) }
}
