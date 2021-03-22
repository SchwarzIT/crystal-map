package kaufland.com.demo.mapper

import androidx.lifecycle.MutableLiveData
import kaufland.com.coachbasebinderapi.mapify.Mapify
import kaufland.com.coachbasebinderapi.mapify.Mapper

@Mapper
class ExposingSource<T> : HiddingSource<T>(){


    @get:Mapify
    @set:Mapify
    var exposedVal : T? = myValue


}