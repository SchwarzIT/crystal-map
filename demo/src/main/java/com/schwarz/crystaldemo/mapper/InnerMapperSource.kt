package com.schwarz.crystaldemo.mapper

import com.schwarz.crystalapi.mapify.Mapify
import com.schwarz.crystalapi.mapify.Mapper

@Mapper
class InnerMapperSource<T, E>(val1: T, val2: E) {
    @Mapify
    val myValue: T = val1

    @Mapify
    val myOtherValue: E = val2
}
