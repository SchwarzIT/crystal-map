package kaufland.com.demo.mapper

import kaufland.com.coachbasebinderapi.mapify.Mapify
import kaufland.com.coachbasebinderapi.mapify.Mapper

@Mapper
class ExposingSource<T>(value: T) : HiddingSource<T>() {

    @get:Mapify
    @set:Mapify
    var exposedVal: T? = myValue
}
