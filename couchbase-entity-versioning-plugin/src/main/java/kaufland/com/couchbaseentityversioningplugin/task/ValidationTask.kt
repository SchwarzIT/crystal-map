package kaufland.com.couchbaseentityversioningplugin.task

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import kaufland.com.coachbasebinderapi.schema.EntitySchema
import kaufland.com.couchbaseentityversioningplugin.SchemaValidationLoggerImpl
import kaufland.com.couchbaseentityversioningplugin.VersioningPluginExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.logging.text.StyledTextOutputFactory
import java.io.File

open class ValidationTask : DefaultTask() {

    @Input
    lateinit var extension: VersioningPluginExtension

    @TaskAction
    fun generate() {

        extension.validationClazz?.let {
            val currentVersionFile = parseVersionSchema(File(extension.currentSchema))
            val prettyPrinter = services.get(StyledTextOutputFactory::class.java)

            val validator = it.newInstance()
            var result = true
            for (versionFile in File(extension.versionedSchemaPath).listFiles()) {
                if (versionFile.extension == "json") {

                    val logger = SchemaValidationLoggerImpl()
                    validator.validate(currentVersionFile, parseVersionSchema(versionFile), logger)
                    logger.print(prettyPrinter.create("model"))
                    result = result && !logger.hasErrors()
                } else {
                    println("skipped validation for ${versionFile.name}")
                }
            }

            if (!result) {
                throw Exception("validation failed")
            }
        } ?: println("no SchemaValidator registered")
    }

    private fun parseVersionSchema(file: File): List<EntitySchema> {
        val mapper = ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).registerModule(KotlinModule())
        return mapper.readValue(file, object : TypeReference<List<EntitySchema>>() {})
    }
}
