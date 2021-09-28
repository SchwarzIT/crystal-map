package kaufland.com.couchbaseentityversioningplugin

import kaufland.com.coachbasebinderapi.schema.SchemaValidator

open class VersioningPluginExtension {
    var currentSchema: String? = null
    var versionedSchemaPath: String? = null
    var validationClazz: Class<SchemaValidator>? = null
}
