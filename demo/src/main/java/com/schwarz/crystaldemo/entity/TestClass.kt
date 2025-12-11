package com.schwarz.crystaldemo.entity

import com.schwarz.crystalapi.BasedOn
import com.schwarz.crystalapi.DocIdSegment
import com.schwarz.crystalapi.Entity
import com.schwarz.crystalapi.Field
import com.schwarz.crystalapi.Fields
import com.schwarz.crystalapi.MapWrapper

@Entity(database = "mydb_db", modifierOpen = true)
@MapWrapper
@Fields(
    Field(name = "type", type = String::class, defaultValue = "something", readonly = true),
    Field(name = "defaultBoolean", type = Boolean::class, defaultValue = "true", readonly = true),
    Field(
        name = "just_some_class_with_a_very_long_name",
        type = JustSomeClassWithAVeryLongName::class,
        list = true,
    ),
    Field(name = "map", type = Map::class, list = true),
)
@BasedOn(AnotherBaseModel::class)
open class TestClass {
    companion object {
        @DocIdSegment
        fun prefix() = "hurra"
    }
}
