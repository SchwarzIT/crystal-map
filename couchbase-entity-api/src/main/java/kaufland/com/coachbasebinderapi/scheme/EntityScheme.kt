package kaufland.com.coachbasebinderapi.scheme

data class EntityScheme(val name: String, val fields: List<Fields>, val basedOn: List<String>, val queries: List<Queries>, val deprecatedScheme: DeprecatedScheme?)

data class Fields(val dbField: String, val fieldType: String, val isIterable : Boolean, val isConstant: Boolean, val defaultValue: String)

data class DeprecatedScheme(val replacedBy : String?, val replaceIn: String?, val deprecatedFields: List<DeprecatedFields>)

data class DeprecatedFields(val field : String, val replacedBy : String?, val replaceIn: String?)

data class Queries(val fields: List<String>)