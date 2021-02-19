package kaufland.com.coachbasebinderapi.schema

interface SchemaValidator {

    fun validate(current: List<EntitySchema>, released: List<EntitySchema>, logger: SchemaValidationLogger)
}