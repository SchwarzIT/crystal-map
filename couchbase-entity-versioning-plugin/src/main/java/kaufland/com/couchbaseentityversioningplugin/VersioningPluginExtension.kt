package kaufland.com.couchbaseentityversioningplugin

import kaufland.com.coachbasebinderapi.scheme.SchemeValidator

open class VersioningPluginExtension {
    var currentScheme : String? = null
    var versionedSchemePath: String? = null
    var validationClazz: Class<SchemeValidator>? = null
}