package kaufland.com.demo.mapper

import kaufland.com.coachbasebinderapi.mapify.IMapifyable
import kaufland.com.coachbasebinderapi.mapify.Mapify
import kaufland.com.coachbasebinderapi.mapify.Mapifyable
import kaufland.com.coachbasebinderapi.mapify.Mapper
import kaufland.com.demo.entity.ProductEntity
import java.io.Serializable
import java.math.BigDecimal

@Mapper
class DummyMapperSource(simple: String = "test123") {

    @Mapify
    private val myPrivateValWithAVeryVeryVeryVeryLongName: String? = simple

    @Mapify
    var innerObject: MyMapifyableTest = MyMapifyableTest(simple)

    @Mapify
    var innerObjectList: List<MyMapifyableTest> = listOf(MyMapifyableTest(simple))

    @Mapify
    var innerObjectMap: Map<String, MyMapifyableTest> = mapOf("test" to MyMapifyableTest(simple))

    @Mapify
    var testSerializable: TestSerializable = TestSerializable(simple, 5)

    @Mapify
    var product: ProductEntity? = null

    @Mapify
    var booleanValue: Boolean = true

    @Mapify
    var bigDecimalValue: BigDecimal? = null

    @Mapify
    val mapper: InnerMapperSource<MyMapifyableTest, String> = InnerMapperSource(MyMapifyableTest(simple), simple)

    @Mapify
    val liveData = ExposingSource<String>()


    val privateValExpose
        get() = myPrivateValWithAVeryVeryVeryVeryLongName

    data class TestSerializable(val test1: String, val test2: Int) : Serializable

    @Mapifyable(MyMapifyableTest.Mapper::class)
    class MyMapifyableTest(val myString: String) {

        class Mapper : IMapifyable<MyMapifyableTest> {
            override fun fromMap(map: Map<String, Any>): MyMapifyableTest {
                return MyMapifyableTest(map["test"] as String)
            }

            override fun toMap(obj: MyMapifyableTest): Map<String, Any> {
                return mapOf("test" to obj.myString)
            }

        }
    }


}