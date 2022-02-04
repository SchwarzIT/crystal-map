package kaufland.com.demo.entity

import kaufland.com.coachbasebinderapi.BaseModel
import kaufland.com.coachbasebinderapi.Field
import kaufland.com.coachbasebinderapi.Fields

@BaseModel
@Fields(
    Field(
        name = "someBaseThing",
        type = String::class,
        defaultValue = "something",
        readonly = true
    ),
    Field(name = "someConstant", type = String::class, defaultValue = "invalid", readonly = true),
)
class BaseModel
