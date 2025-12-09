import com.schwarz.crystalapi.mapify.IMapifyable
import com.schwarz.crystalapi.mapify.Mapify
import com.schwarz.crystalapi.mapify.Mapifyable
import com.schwarz.crystalapi.mapify.Mapper
import java.io.Serializable

@Mapper
class MapperWithNullable {

    @Mapify(nullableIndexes = [1])
    private val names : List<String?> = listOf()

    @Mapify(nullableIndexes = [1, 2])
    private val namesWithAge : Map<String?, Int?> = mapOf()
}