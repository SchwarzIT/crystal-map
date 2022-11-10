package com.schwarz.crystaldemo.entity

import com.schwarz.crystalapi.*
import com.schwarz.crystaldemo.customtypes.GenerateClassName

@com.schwarz.crystalapi.BaseModel
@MapWrapper
@Entity
@Fields(
    Field(name = "someConstant", type = String::class, defaultValue = "blabla", readonly = true),
    Field(name = "anotherBaseThing", type = String::class),
    Field(name = "clazzName", type = GenerateClassName::class, defaultValue = "GenerateClassName(this::class.simpleName ?: \"\")")
)
@BasedOn(BaseModel::class)
open class AnotherBaseModel
