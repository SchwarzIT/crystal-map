package com.schwarz.crystaldemo.entity

import com.schwarz.crystalapi.Fields
import com.schwarz.crystalapi.MapWrapper
import com.schwarz.crystalapi.Field

@MapWrapper
@Fields(
    Field(name = "comment", type = String::class),
    Field(name = "user", type = String::class, defaultValue = "anonymous"),
    Field("age", type = Int::class, defaultValue = "0")
)
open class UserComment
