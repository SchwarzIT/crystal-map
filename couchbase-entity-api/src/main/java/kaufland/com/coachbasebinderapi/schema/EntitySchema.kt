package kaufland.com.coachbasebinderapi.schema

data class EntitySchema(val name: String, val fields: List<Fields>, val basedOn: List<String>, val queries: List<Queries>, val docId: DocId?, val deprecatedSchema: DeprecatedSchema?)

data class Fields(val dbField: String, val fieldType: String, val isIterable : Boolean, val isConstant: Boolean, val defaultValue: String)

data class DeprecatedSchema(val replacedBy : String?, val inUse: Boolean, val deprecatedFields: List<DeprecatedFields>)

data class DeprecatedFields(val field : String, val replacedBy : String?, val inUse: Boolean)

data class Queries(val fields: List<String>)

data class DocId(val scheme: String)