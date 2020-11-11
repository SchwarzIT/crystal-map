package kaufland.com.demo.entity

import kaufland.com.coachbasebinderapi.BaseModel
import kaufland.com.coachbasebinderapi.Field
import kaufland.com.coachbasebinderapi.Fields

@BaseModel
@Fields(
        Field(name = "anotherBaseThing", type = String::class)
)
class AnotherBaseModel