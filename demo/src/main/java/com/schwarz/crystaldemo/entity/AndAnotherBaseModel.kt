package com.schwarz.crystaldemo.entity

import com.schwarz.crystalapi.BasedOn
import com.schwarz.crystalapi.Entity
import com.schwarz.crystalapi.Field
import com.schwarz.crystalapi.Fields
import com.schwarz.crystalapi.MapWrapper
import com.schwarz.crystaldemo.customtypes.GenerateClassName

@com.schwarz.crystalapi.BaseModel
@MapWrapper
@Entity
@Fields(
    Field(name = "andAnotherBaseThing", type = String::class),
    Field(
        name = "clazzName",
        type = GenerateClassName::class,
        defaultValue = "GenerateClassName(this::class.simpleName ?: \"\")",
    ),
)
@BasedOn(AnotherBaseModel::class)
open class AndAnotherBaseModel
