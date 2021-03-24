import kaufland.com.coachbasebinderapi.mapify.IMapifyable
import kaufland.com.coachbasebinderapi.mapify.Mapify
import kaufland.com.coachbasebinderapi.mapify.Mapifyable
import kaufland.com.coachbasebinderapi.mapify.Mapper
import java.io.Serializable

@Mapper
class MapperWithNullable {

    @Mapify(nullableIndexes = [1])
    private val names : List<String?> = listOf()

    @Mapify(nullableIndexes = [1, 2])
    private val namesWithAge : Map<String?, Int?> = mapOf()
}