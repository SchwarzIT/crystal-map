package com.schwarz.crystaldummy.customtypes

import com.schwarz.crystalapi.ITypeConverter
import com.schwarz.crystalapi.TypeConverter
import java.time.Instant
import java.util.Date

@TypeConverter
abstract class DummyDateConverter : ITypeConverter<Date, Number> {
    override fun write(value: Date?): Number? =
        value?.toInstant()?.epochSecond

    override fun read(value: Number?): Date? = value?.toLong()?.let { Date.from(Instant.ofEpochSecond(it)) }
}
