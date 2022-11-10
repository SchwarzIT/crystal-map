import com.schwarz.crystalapi.mapify.Mapify
import com.schwarz.crystalapi.mapify.Mapper

@Mapper
class MapperWithGetterAndSetter<T>{


    @get:Mapify
    @set:Mapify
    private var exposedVal : T?
        get() =innerVal
        set(value) { value.let { innerVal = it }}

            private var innerVal : T? = null

}