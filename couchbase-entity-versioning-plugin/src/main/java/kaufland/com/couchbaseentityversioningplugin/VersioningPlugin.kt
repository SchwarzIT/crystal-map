package kaufland.com.couchbaseentityversioningplugin

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import kaufland.com.coachbasebinderapi.scheme.EntityScheme
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

class VersioningPlugin : Plugin<Project> {



    override fun apply(project: Project) {
        val extension = project.extensions.create("couchbaseEntityVersioning", VersioningPluginExtension::class.java)

        project.task("markCurrentSchemeAsReleased") {
            it.doLast {

                val version : String = when{
                    project.hasProperty("entity-version") -> project.property("entity-version") as String
                    else -> System.console().readLine("insert version of release")
                }

                val currentVersionFile = File(extension.currentScheme)

                File(extension.versionedSchemePath).mkdirs()
                val target = File(extension.versionedSchemePath, "$version.json")
                try {
                    currentVersionFile.copyTo(target)
                    println("added new version")
                } catch (fnf: NoSuchFileException) {
                    throw Exception("source file not exists ${extension.currentScheme}")
                } catch (fae: FileAlreadyExistsException) {
                    throw Exception("version already exists ${target.absolutePath}")
                }
            }
        }
        project.task("removeScheme") {
            it.doLast {
                val version : String = when{
                    project.hasProperty("entity-version") -> project.property("entity-version") as String
                    else -> System.console().readLine("insert version to remove")
                }

                val target = File(extension.versionedSchemePath, "$version.json")
                try {
                    target.delete()
                    println("version removed")
                } catch (fnf: NoSuchFileException) {
                    println("version not exists ${target.absolutePath}")
                }
            }
        }
        project.task("validateScheme") {
            it.doLast { task ->

                extension.validationClazz?.let {
                    val currentVersionFile = parseVersionScheme(File(extension.currentScheme))

                    val validator = it.newInstance()
                    var result = true
                    for (versionFile in File(extension.versionedSchemePath).listFiles()) {
                        val version = versionFile.nameWithoutExtension
                        if (versionFile.extension == "json") {
                            result = result && validator.validate(currentVersionFile, parseVersionScheme(versionFile), version)
                        } else {
                            println("skipped validation for ${versionFile.name}")
                        }
                    }

                    if (!result) {
                        throw Exception("validation failed")
                    }

                } ?: println("no SchemeValidator registered")

            }
        }
    }

    private fun parseVersionScheme(file: File): List<EntityScheme> {
        val mapper = ObjectMapper().registerModule(KotlinModule())
        return mapper.readValue(file, object : TypeReference<List<EntityScheme>>() {})
    }
}