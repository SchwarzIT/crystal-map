import com.schwarz.crystalapi.mapify.IMapifyable
import com.schwarz.crystalapi.mapify.Mapify
import com.schwarz.crystalapi.mapify.Mapifyable
import com.schwarz.crystalapi.mapify.Mapper
import java.io.Serializable

@Mapper
class SimpleMapperTest {

    @Mapify
    private val name : String? = ""

    @Mapify
    private val innerObject : MyMapifyableTest = MyMapifyableTest()

    @Mapify
    private val listInnerObject : List<MyMapifyableTest> = listOf(MyMapifyableTest(), MyMapifyableTest())

    @Mapify
    private val testSerializable : TestSerializable = TestSerializable("test123", 5)

    @Mapify
    private val booleanValue: Boolean = true

    data class TestSerializable(val test1: String, val test2: Int) : Serializable

    @Mapifyable(MyMapifyableTest.Mapper::class)
    class MyMapifyableTest {

        class Mapper : IMapifyable<MyMapifyableTest> {
            override fun fromMap(map: Map<String, Any>): MyMapifyableTest {
                return MyMapifyableTest()
            }

            override fun toMap(obj: MyMapifyableTest): Map<String, Any> {
                return mapOf()
            }

        }
    }
}