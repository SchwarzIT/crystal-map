package com.schwarz.crystalprocessor

import com.schwarz.crystalcore.ISettings
import javax.annotation.processing.ProcessingEnvironment

class ProcessingEnvironmentWrapper(private val processingEnvironment: ProcessingEnvironment) : ISettings {
    override val kotlinGeneratedPath: String? = processingEnvironment.options[CoachBaseBinderProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME]
    override val documentationPath: String? = processingEnvironment.options[CoachBaseBinderProcessor.FRAMEWORK_DOCUMENTATION_PATH_OPTION_NAME]
    override val documentationFilename: String? = processingEnvironment.options[CoachBaseBinderProcessor.FRAMEWORK_DOCUMENTATION_FILENAME_OPTION_NAME]
    override val schemaPath: String? = processingEnvironment.options[CoachBaseBinderProcessor.FRAMEWORK_SCHEMA_PATH_OPTION_NAME]
    override val schemaFilename: String? = processingEnvironment.options[CoachBaseBinderProcessor.FRAMEWORK_SCHEMA_FILENAME_OPTION_NAME]
    override val entityRelationshipPath: String? = processingEnvironment.options[CoachBaseBinderProcessor.FRAMEWORK_ENTITY_RELATIONSHIP_PATH_OPTION_NAME]
    override val entityRelationshipFilename: String? = processingEnvironment.options[CoachBaseBinderProcessor.FRAMEWORK_ENTITY_RELATIONSHIP_FILENAME_OPTION_NAME]
}
