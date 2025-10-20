package com.schwarz.crystalksp

import com.schwarz.crystalcore.ISettings

class ProcessingEnvironmentWrapper(private val options: Map<String, String>) :
    ISettings {

    override val kotlinGeneratedPath: String? = options[CrystalProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME]
    override val documentationPath: String? = options[CrystalProcessor.FRAMEWORK_DOCUMENTATION_PATH_OPTION_NAME]
    override val documentationFilename: String? = options[CrystalProcessor.FRAMEWORK_DOCUMENTATION_FILENAME_OPTION_NAME]
    override val schemaPath: String? = options[CrystalProcessor.FRAMEWORK_SCHEMA_PATH_OPTION_NAME]
    override val schemaFilename: String? = options[CrystalProcessor.FRAMEWORK_SCHEMA_FILENAME_OPTION_NAME]
    override val entityRelationshipPath: String? = options[CrystalProcessor.FRAMEWORK_ENTITY_RELATIONSHIP_PATH_OPTION_NAME]
    override val entityRelationshipFilename: String? = options[CrystalProcessor.FRAMEWORK_ENTITY_RELATIONSHIP_FILENAME_OPTION_NAME]

    val useSuspend: Boolean? = options[CrystalProcessor.FRAMEWORK_USE_SUSPEND_OPTION_NAME]?.toBoolean()
}
