import com.schwarz.crystalapi.mapify.IMapifyable
import com.schwarz.crystalapi.mapify.Mapify
import com.schwarz.crystalapi.mapify.Mapifyable
import com.schwarz.crystalapi.mapify.Mapper
import java.io.Serializable

@Mapper
class MapperWithTypeParam<T> {

    @Mapify
    private val name : T? = null
}