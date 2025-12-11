package com.schwarz.crystalapi.schema

import kotlinx.serialization.Serializable

@Serializable
data class EntitySchema(
    val name: String,
    val fields: List<Fields>,
    val basedOn: List<String>,
    val queries: List<Queries>,
    val docId: DocId?,
    val deprecatedSchema: DeprecatedSchema?,
) {
    fun toMap(): Map<String, Any?> =
        mapOf(
            "name" to name,
            "fields" to fields.map { it.toMap() },
            "basedOn" to basedOn,
            "queries" to queries.map { it.fields },
            "docId" to docId?.toMap(),
            "deprecatedSchema" to deprecatedSchema?.toMap(),
        )
}

@Serializable
data class Fields(
    val dbField: String,
    val fieldType: String,
    val isIterable: Boolean,
    val isConstant: Boolean,
    val defaultValue: String,
    val mandatory: Boolean?,
) {
    fun toMap(): Map<String, Any?> =
        mapOf(
            "dbField" to dbField,
            "fieldType" to fieldType,
            "isIterable" to isIterable,
            "isConstant" to isConstant,
            "defaultValue" to defaultValue,
            "mandatory" to mandatory,
        )
}

@Serializable
data class DeprecatedSchema(
    val replacedBy: String?,
    val inUse: Boolean,
    val deprecatedFields: List<DeprecatedFields>,
) {
    fun toMap(): Map<String, Any?> =
        mapOf(
            "replacedBy" to replacedBy,
            "inUse" to inUse,
            "deprecatedFields" to deprecatedFields.map { it.toMap() },
        )
}

@Serializable
data class DeprecatedFields(
    val field: String,
    val replacedBy: String?,
    val inUse: Boolean,
) {
    fun toMap(): Map<String, Any?> =
        mapOf(
            "field" to field,
            "replacedBy" to replacedBy,
            "inUse" to inUse,
        )
}

@Serializable
data class Queries(
    val fields: List<String>,
) {
    fun toMap(): Map<String, Any?> =
        mapOf(
            "fields" to fields,
        )
}

@Serializable
data class DocId(
    val scheme: String,
) {
    fun toMap(): Map<String, Any?> =
        mapOf(
            "scheme" to scheme,
        )
}
