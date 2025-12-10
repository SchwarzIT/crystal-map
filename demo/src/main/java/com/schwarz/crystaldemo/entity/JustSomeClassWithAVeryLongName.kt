package com.schwarz.crystaldemo.entity

import com.schwarz.crystalapi.Entity
import com.schwarz.crystalapi.Field
import com.schwarz.crystalapi.Fields
import com.schwarz.crystalapi.MapWrapper

@Entity(database = "mydb_db")
@MapWrapper
@Fields(
    Field(name = "type", type = String::class, defaultValue = "something", readonly = true),
    Field(name = "name", type = String::class),
)
open class JustSomeClassWithAVeryLongName
