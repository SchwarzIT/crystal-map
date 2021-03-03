package kaufland.com.demo.mapper

import kaufland.com.coachbasebinderapi.PersistenceConfig
import kaufland.com.coachbasebinderapi.TypeConversion
import org.junit.Assert
import org.junit.Assert.*
import org.junit.BeforeClass
import org.junit.Test
import kotlin.reflect.KClass

class DummyMapperSourceTest {
    @Test
    fun `check toMap`() {
        val mapper = DummyMapperSourceMapper()
        val obj = DummyMapperSource("oldValue")

        val mapToPersist = mapper.toMap(obj)

        val newObj = DummyMapperSource("newValue")

        Assert.assertEquals("newValue", newObj.myPrivateVal)
        Assert.assertEquals("newValue", newObj.testSerializable.test1)
        mapper.fromMap(newObj, mapToPersist)
        Assert.assertEquals("oldValue", newObj.myPrivateVal)
        Assert.assertEquals("oldValue", newObj.testSerializable.test1)


    }
}
