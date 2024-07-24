package com.schwarz.crystaldemo.customtypes

import com.schwarz.crystalapi.ITypeConverter
import com.schwarz.crystalapi.TypeConverter
import java.time.LocalDate

@TypeConverter
abstract class LocalDateConverter : ITypeConverter<LocalDate, String> {
    override fun write(value: LocalDate?): String? =
        value?.toString()

    override fun read(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }
}
