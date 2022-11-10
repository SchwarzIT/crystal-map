package com.schwarz.crystaldemo.entity

import com.schwarz.crystalapi.Field
import com.schwarz.crystalapi.Fields

@com.schwarz.crystalapi.BaseModel
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
