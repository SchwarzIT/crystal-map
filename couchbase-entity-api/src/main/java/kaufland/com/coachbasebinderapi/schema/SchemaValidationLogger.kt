package kaufland.com.coachbasebinderapi.schema

interface SchemaValidationLogger {

    fun info(entitySchema: EntitySchema, message : String)

    fun error(entitySchema: EntitySchema, message : String)

    fun warning(entitySchema: EntitySchema, message : String)


}