package com.schwarz.crystaldemo.mapper

import com.schwarz.crystalapi.mapify.Mapify
import com.schwarz.crystalapi.mapify.Mapper

@Mapper
class ExposingSource<T>(value: T) : HiddingSource<T>() {

    @Mapify
    var exposedVal: T? = myValue
}
