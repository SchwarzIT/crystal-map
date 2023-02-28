package com.schwarz.crystaldemo.entity

import com.schwarz.crystalapi.*
import com.schwarz.crystaldemo.customtypes.GenerateClassName

@com.schwarz.crystalapi.BaseModel
@MapWrapper
@Entity
@Fields(
    Field(name = "andAnotherBaseThing", type = String::class),
    Field(name = "clazzName", type = GenerateClassName::class, defaultValue = "GenerateClassName(this::class.simpleName ?: \"\")")
)
@BasedOn(AnotherBaseModel::class)
open class AndAnotherBaseModel
