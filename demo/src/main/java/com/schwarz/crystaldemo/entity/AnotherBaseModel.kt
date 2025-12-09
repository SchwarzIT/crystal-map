package com.schwarz.crystaldemo.entity

import com.schwarz.crystalapi.*
import com.schwarz.crystaldemo.customtypes.GenerateClassName

typealias AnotherBaseThing = String

@com.schwarz.crystalapi.BaseModel
@MapWrapper
@Entity
@Fields(
    Field(name = "someConstant", type = String::class, defaultValue = "blabla", readonly = true),
    Field(name = "anotherBaseThing", type = AnotherBaseThing::class),
    Field(name = "clazzName", type = GenerateClassName::class, defaultValue = "GenerateClassName(this::class.simpleName ?: \"\")")
)
@BasedOn(BaseModel::class)
open class AnotherBaseModel
