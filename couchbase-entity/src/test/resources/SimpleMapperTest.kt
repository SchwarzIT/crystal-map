import kaufland.com.coachbasebinderapi.mapify.IMapifyable
import kaufland.com.coachbasebinderapi.mapify.Mapify
import kaufland.com.coachbasebinderapi.mapify.Mapifyable
import kaufland.com.coachbasebinderapi.mapify.Mapper

@Mapper
class SimpleMapperTest {

    @Mapify
    private val name : String = ""

    @Mapify
    private val innerObject : MyMapifyableTest = MyMapifyableTest()

    @Mapify
    private val listInnerObject : List<MyMapifyableTest> = listOf(MyMapifyableTest(), MyMapifyableTest())

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