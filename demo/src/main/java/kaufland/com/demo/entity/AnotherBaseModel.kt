package kaufland.com.demo.entity

import kaufland.com.coachbasebinderapi.*
import kaufland.com.coachbasebinderapi.BaseModel
import kaufland.com.demo.customtypes.GenerateClassName

@BaseModel
@MapWrapper
@Entity
@Fields(
        Field(name = "someConstant", type = String::class, defaultValue = "blabla", readonly = true),
        Field(name = "anotherBaseThing", type = String::class),
        Field(name = "clazzName", type = GenerateClassName::class, defaultValue = "GenerateClassName(this::class.simpleName ?: \"\")")
)
@BasedOn(kaufland.com.demo.entity.BaseModel::class)
open class AnotherBaseModel{

}
