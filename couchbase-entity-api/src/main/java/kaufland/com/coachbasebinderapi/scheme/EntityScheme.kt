package kaufland.com.coachbasebinderapi.scheme

data class EntityScheme(val name: String, val fields: List<Fields>, val basedOn: List<String>, val queries: List<Queries>, val docId: DocId?, val deprecatedScheme: DeprecatedScheme?)

data class Fields(val dbField: String, val fieldType: String, val isIterable : Boolean, val isConstant: Boolean, val defaultValue: String)

data class DeprecatedScheme(val replacedBy : String?, val inUse: Boolean, val deprecatedFields: List<DeprecatedFields>)

data class DeprecatedFields(val field : String, val replacedBy : String?, val inUse: Boolean)

data class Queries(val fields: List<String>)

data class DocId(val scheme: String)