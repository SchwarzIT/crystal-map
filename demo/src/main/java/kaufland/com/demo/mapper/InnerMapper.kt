package kaufland.com.demo.mapper

import kaufland.com.coachbasebinderapi.mapify.Mapify
import kaufland.com.coachbasebinderapi.mapify.Mapper

@Mapper
class InnerMapper<T> {

    @Mapify
    val myValue: T? = null

}