import com.schwarz.crystalapi.*
import com.schwarz.crystalapi.typeconverters.EnumConverter
import java.lang.String

enum class TestEnum {
        TEST_VALUE1,
        TEST_VALUE2
}

@TypeConverter
open class TestEnumConverter: ITypeConverter<TestEnum, kotlin.String> by EnumConverter(TestEnum::class)

@Entity(database = "mydb_db")
@Fields(
        Field(name = "name", type = String::class),
        Field(name = "type", type = String::class, defaultValue = "entityWithQueries", readonly = true),
        Field(name = "identifiers", type = String::class, list = true),
        Field(name = "enumField", type = TestEnum::class),
        Field(name = "map", type = Map::class, list = true)
)
@DocId("my:%name%:%type%:%enumField%")
open class EntityWithEnumDocId {

}
