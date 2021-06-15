package kaufland.com.demo.entity

import kaufland.com.coachbasebinderapi.BaseModel
import kaufland.com.coachbasebinderapi.BasedOn
import kaufland.com.coachbasebinderapi.Field
import kaufland.com.coachbasebinderapi.Fields
import kaufland.com.demo.customtypes.GenerateClassName

@BaseModel
@Fields(
        Field(name = "anotherBaseThing", type = String::class),
        Field(name = "clazzName", type = GenerateClassName::class, defaultValue = "GenerateClassName(this::class.simpleName ?: \"\")")
)
@BasedOn(kaufland.com.demo.entity.BaseModel::class)
class AnotherBaseModel
