package kaufland.com.demo.mapper

import kaufland.com.coachbasebinderapi.mapify.Mapify
import kaufland.com.coachbasebinderapi.mapify.Mapper

@Mapper
class InnerMapperSource<T, E>(val1: T, val2: E) {

    @Mapify
    val myValue: T = val1

    @Mapify
    val myOtherValue: E = val2
}
