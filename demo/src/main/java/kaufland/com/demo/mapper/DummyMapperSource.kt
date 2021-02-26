package kaufland.com.demo.mapper

import kaufland.com.coachbasebinderapi.mapify.IMapifyable
import kaufland.com.coachbasebinderapi.mapify.Mapify
import kaufland.com.coachbasebinderapi.mapify.Mapifyable
import kaufland.com.coachbasebinderapi.mapify.Mapper
import java.io.Serializable

@Mapper
class DummyMapperSource {

    @Mapify
    private val myPrivateVal = "test123"

    @Mapify
    private val innerObject : MyMapifyableTest = MyMapifyableTest()

    @Mapify
    private val innerObjectList : List<MyMapifyableTest> = listOf(MyMapifyableTest())

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