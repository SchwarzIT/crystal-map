package kaufland.com.demo.entity

import kaufland.com.coachbasebinderapi.Fields
import kaufland.com.coachbasebinderapi.MapWrapper
import kaufland.com.coachbasebinderapi.Field

@MapWrapper
@Fields(
    Field(name = "comment", type = String::class),
    Field(name = "user", type = String::class, defaultValue = "anonymous"),
    Field("age", type = Int::class, defaultValue = "0")
)
open class UserComment
