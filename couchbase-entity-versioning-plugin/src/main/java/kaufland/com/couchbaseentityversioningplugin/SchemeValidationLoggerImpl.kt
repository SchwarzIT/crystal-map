package kaufland.com.couchbaseentityversioningplugin

import kaufland.com.coachbasebinderapi.scheme.EntityScheme
import kaufland.com.coachbasebinderapi.scheme.SchemeValidationLogger
import org.gradle.internal.logging.text.StyledTextOutput

class SchemeValidationLoggerImpl : SchemeValidationLogger {

    private data class Message(val message: String, val level: Level)

    private enum class Level {
        INFO,
        WARNING,
        ERROR
    }

    private val map: MutableMap<EntityScheme, MutableList<Message>> = HashMap()


    override fun info(entityScheme: EntityScheme, message: String) {
        add(entityScheme, message, Level.INFO)
    }

    override fun error(entityScheme: EntityScheme, message: String) {
        add(entityScheme, message, Level.ERROR)
    }

    override fun warning(entityScheme: EntityScheme, message: String) {
        add(entityScheme, message, Level.WARNING)
    }

    private fun add(entityScheme: EntityScheme, message: String, level: Level) {
        map.computeIfAbsent(entityScheme) {
            mutableListOf()
        }
        map[entityScheme]?.add(Message(message, level))
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