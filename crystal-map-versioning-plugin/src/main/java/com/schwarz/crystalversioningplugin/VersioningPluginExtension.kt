package com.schwarz.crystalversioningplugin

import com.schwarz.crystalapi.schema.SchemaValidator

open class VersioningPluginExtension {
    var currentSchema: String? = null
    var versionedSchemaPath: String? = null
    var validationClazz: Class<SchemaValidator>? = null
}
