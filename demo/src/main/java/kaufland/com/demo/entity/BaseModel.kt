package kaufland.com.demo.entity

import kaufland.com.coachbasebinderapi.BaseModel
import kaufland.com.coachbasebinderapi.Field
import kaufland.com.coachbasebinderapi.Fields

@BaseModel
@Fields(
    Field(name = "someBaseThing", type = String::class, defaultValue = "something", readonly = true)
)
class BaseModel
