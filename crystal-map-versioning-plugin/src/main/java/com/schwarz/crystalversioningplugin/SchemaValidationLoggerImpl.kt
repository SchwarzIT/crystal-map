package kaufland.com.couchbaseentityversioningplugin

import com.schwarz.crystalapi.schema.EntitySchema
import com.schwarz.crystalapi.schema.SchemaValidationLogger
import org.gradle.internal.logging.text.StyledTextOutput

class SchemaValidationLoggerImpl : SchemaValidationLogger {

    private data class Message(val message: String, val level: Level)

    private enum class Level {
        INFO,
        WARNING,
        ERROR
    }

    private val map: MutableMap<EntitySchema, MutableList<Message>> = HashMap()

    override fun info(entitySchema: EntitySchema, message: String) {
        add(entitySchema, message, Level.INFO)
    }

    override fun error(entitySchema: EntitySchema, message: String) {
        add(entitySchema, message, Level.ERROR)
    }

    override fun warning(entitySchema: EntitySchema, message: String) {
        add(entitySchema, message, Level.WARNING)
    }

    private fun add(entitySchema: EntitySchema, message: String, level: Level) {
        map.computeIfAbsent(entitySchema) {
            mutableListOf()
        }
        map[entitySchema]?.add(Message(message, level))
    }

    fun print(printer: StyledTextOutput) {
        for (entry in map) {
            printer.withStyle(StyledTextOutput.Style.Header)
                .println("")
                .println("Validating [${entry.key.name}]")

            for (message in entry.value) {
                printer.style(StyledTextOutput.Style.Normal).text("--  ${message.message} ").printSuffixLn(message.level)
            }
        }
    }

    private fun StyledTextOutput.printSuffixLn(level: Level) = when (level) {
        Level.ERROR -> style(StyledTextOutput.Style.Error).println("[FAILED]")
        Level.WARNING -> style(StyledTextOutput.Style.ProgressStatus).println("[WARN]")
        else -> style(StyledTextOutput.Style.Identifier).println("[OK]")
    }

    fun hasErrors(): Boolean {
        return map.values.flatMap { it }.any { it.level == Level.ERROR }
    }
}
