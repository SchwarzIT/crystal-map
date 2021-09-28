package kaufland.com.demo.entity

import kaufland.com.coachbasebinderapi.*

@Entity(database = "mydb_db")
@MapWrapper
@Fields(
    Field(name = "type", type = String::class, defaultValue = "something", readonly = true),
    Field(name = "name", type = String::class)
)
open class JustSomeClassWithAVeryLongName
