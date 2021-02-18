package kaufland.com.couchbaseentityversioningplugin

import kaufland.com.couchbaseentityversioningplugin.task.ValidationTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import java.io.File

private const val EXTENSION_NAME = "couchbaseEntityVersioning"

private const val TASK_VALIDATE_SCHEME = "validateScheme"
private const val TASK_REMOVE_SCHEME = "removeScheme"
private const val TASK_ADD_SCHEME = "addScheme"

private const val PARAM_VERSION = "entity-version"

class VersioningPlugin : Plugin<Project> {


    override fun apply(project: Project) {
        val extension = project.extensions.create(EXTENSION_NAME, VersioningPluginExtension::class.java)

        project.run {
            tasks.register(TASK_VALIDATE_SCHEME, ValidationTask::class.java){
                it.extension = extension
            }
            tasks.register(TASK_REMOVE_SCHEME) {
                removeScheme(project, extension, it)
            }
            tasks.register(TASK_ADD_SCHEME) {
                markCurrentSchemeAsReleased(project, extension, it)
            }
        }
    }


    private fun markCurrentSchemeAsReleased(project: Project, extension: VersioningPluginExtension, task: Task) {
        task.doLast {

            val version: String = when {
                project.hasProperty(PARAM_VERSION) -> project.property(PARAM_VERSION) as String
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

    private fun removeScheme(project: Project, extension: VersioningPluginExtension, task: Task) {
        task.doLast {
            val version: String = when {
                project.hasProperty(PARAM_VERSION) -> project.property(PARAM_VERSION) as String
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

}